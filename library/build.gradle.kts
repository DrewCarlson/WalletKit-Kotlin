import com.android.build.gradle.LibraryExtension

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
}

val ideaActive = System.getProperty("idea.active") == "true"
val hasAndroid = System.getProperty("sdk.dir") != null || System.getenv("ANDROID_HOME") != null

apply(from = "$rootDir/gradle/publishing.gradle.kts")
apply(from = "../gradle/native-model.gradle")
apply(plugin = "kotlinx-atomicfu")

val corePath = rootProject.file("walletkit/WalletKitCore").absolutePath
val cppCryptoSrcDir = "$corePath/src"
val cppCryptoIncludeDir = "$corePath/include"

if (hasAndroid) {
    apply(plugin = "com.android.library")
    configure<LibraryExtension> {
        compileSdk = 28
        defaultConfig {
            minSdk = 23
            targetSdk = 28
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            externalNativeBuild {
                cmake.arguments("-DANDROID_STL=none", "-DANDROID_TOOLCHAIN=clang")
            }
            ndk.abiFilters.addAll(listOf("x86", "x86_64", "armeabi-v7a", "arm64-v8a"))
        }

        externalNativeBuild {
            cmake {
                path = rootProject.file("walletkit/WalletKitCore/CMakeLists.txt")
                version = "3.10.2"
            }
        }

        sourceSets {
            named("main") {
                java.srcDir(rootProject.file("walletkit/WalletKitJava/WalletKitNative/src/main/java"))
            }
        }
    }
}

val publicApiHeaders = (project.file("$corePath/include").listFiles() ?: emptyArray())
        .filter { it.name.endsWith(".h") }
val privateApiHeaders = (project.file("$corePath/src/crypto").listFiles() ?: emptyArray())
        .filter { it.name.endsWith(".h") }

apiValidation {
    ignoredPackages.add("com.blockset.walletkit.nativex")
}

kotlin {
    val wkn = gradle.includedBuild("WalletKitJava")
    ios()
    jvm {
        compilations.all {
            // TODO: Build with native-model.gradle tasks
            compileKotlinTask
                    .dependsOn(wkn.task(":WalletKitNative-JRE:blake2SharedLibrary"))
                    .dependsOn(wkn.task(":WalletKitNative-JRE:ed25519SharedLibrary"))
                    .dependsOn(wkn.task(":WalletKitNative-JRE:sqlite3SharedLibrary"))
                    .dependsOn(wkn.task(":WalletKitNative-JRE:WalletKitCoreSharedLibrary"))
        }
    }
    if (hasAndroid) {
        android { publishAllLibraryVariants() }
    }

    val nativeTargets = listOf(
            macosX64("macos"),
            iosX64(),
            iosArm64(),
    )

    configure(nativeTargets) {
        // TODO: Resolve linking paths from native build plugin
        val coreStaticPath = project.file("build/libs/walletKitCore/static/$name").absolutePath
        val sqliteStaticPath = project.file("build/libs/sqlite3/static/$name").absolutePath
        val ed25519StaticPath = project.file("build/libs/ed25519/static/$name").absolutePath
        val blake2StaticPath = project.file("build/libs/blake2/static/$name").absolutePath
        val darwinLinkerOpts = listOf(
                "-L$coreStaticPath",
                "-L$sqliteStaticPath",
                "-L$ed25519StaticPath",
                "-L$blake2StaticPath",
                "-framework", "Security",
        )
        binaries.getTest(DEBUG).apply {
            linkerOpts(darwinLinkerOpts)
        }

        if (name.startsWith("ios") || name.startsWith("macos")) {
            // Create .frameworks for use in Obj-c/Swift projects
            binaries.framework {
                linkerOpts(darwinLinkerOpts)
            }
        }

        compilations.getByName("main") {
            // Link native library compilation to root kotlin compilation
            /*project(":libs").subprojects.forEach { lib ->
                lib.tasks.whenTaskAdded {
                    if (name.startsWith("assemble") && name.endsWith(targetName, true)) {
                        compileKotlinTask.dependsOn(path)
                    }
                }
            }*/
            kotlinOptions {
                freeCompilerArgs += listOf(
                        "-include-binary", "$coreStaticPath/libWalletKitCore.a",
                        "-include-binary", "$sqliteStaticPath/libsqlite3.a",
                        "-include-binary", "$ed25519StaticPath/libed25519.a",
                        "-include-binary", "$blake2StaticPath/libblake2.a",
                        //"-memory-model", "experimental",
                        "-Xallocator=mimalloc"
                )
            }
            val WalletKitCore by cinterops.creating {
                packageName = "walletkit.core"
                headers(publicApiHeaders)
                headers(privateApiHeaders)
                includeDirs(cppCryptoSrcDir, cppCryptoIncludeDir)
            }
        }
    }

    sourceSets {
        all {
            languageSettings.apply {
                optIn("kotlin.RequiresOptIn")
                optIn("kotlin.ExperimentalUnsignedTypes")
                explicitApi()
            }
        }

        val commonMain by getting {
            dependencies {
                api("org.drewcarlson:blockset:$BLOCKSET_VERSION")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$COROUTINES_VERSION")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$SERIALIZATION_VERSION")
                implementation("io.ktor:ktor-client-core:$KTOR_VERSION")
                implementation("io.ktor:ktor-client-json:$KTOR_VERSION")
                implementation("io.ktor:ktor-client-serialization:$KTOR_VERSION")
                implementation("io.ktor:ktor-client-logging:$KTOR_VERSION")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmCommonMain by creating {
            targetFromPreset(presets["jvm"])
            dependsOn(commonMain)
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:$KTOR_VERSION")

                compileOnly("com.blockset.walletkit:WalletKitNative-JRE")
            }
        }

        val jvmCommonTest by creating {
            dependsOn(commonTest)
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }

        val jvmMain by getting {
            dependsOn(jvmCommonMain)
            dependencies {
                implementation("com.blockset.walletkit:WalletKitNative-JRE")
            }
        }

        val jvmTest by getting {
            dependsOn(jvmCommonTest)
            // TODO: Link jvm compile step to native-model.gradle task output
            resources.srcDirs("../walletkit/WalletKitJava/WalletKitNative-JRE/build/resources/main")
        }

        if (hasAndroid) {
            val androidMain by getting {
                dependsOn(jvmCommonMain)
                dependencies {
                    implementation("net.java.dev.jna:jna:${libs.versions.jnaAndroid.get()}")
                    implementation(libs.guava.android)
                }
            }

            val androidTest by getting {
                dependsOn(jvmCommonTest)
                dependencies {
                    implementation("androidx.test:runner:1.4.0")
                }
            }
        }

        val darwinCommonMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation("co.touchlab:stately-iso-collections:$STATELY_VERSION")
            }
        }

        val iosMain by getting {
            dependsOn(darwinCommonMain)
            dependencies {
                implementation("io.ktor:ktor-client-ios:$KTOR_VERSION")
            }
        }

        val macosMain by getting {
            dependsOn(darwinCommonMain)
            dependencies {
                implementation("io.ktor:ktor-client-curl:$KTOR_VERSION")
            }
        }
    }
}
