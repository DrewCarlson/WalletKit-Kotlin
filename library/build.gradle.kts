import com.android.build.gradle.LibraryExtension

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.dokka")
}

val ideaActive = System.getProperty("idea.active") == "true"
val hasAndroid = false// TODO: fixme System.getProperty("sdk.dir") != null || System.getenv("ANDROID_HOME") != null

apply(plugin = "kotlinx-atomicfu")

val corePath = rootProject.file("walletkit/WalletKitCore").absolutePath
val coreLibPath = rootProject.file("libs/corecrypto/build/lib/main").absolutePath
val sqliteLibPath = rootProject.file("libs/sqlite3/build/lib/main").absolutePath
val ed25519LibPath = rootProject.file("libs/ed25519/build/lib/main").absolutePath
val blake2LibPath = rootProject.file("libs/blake2/build/lib/main").absolutePath

val cppCryptoSrcDir = "$corePath/src"
val cppCryptoIncludeDir = "$corePath/include"
val cppCryptoSrcDirs = listOf(
        "bcash",
        "bitcoin",
        "bsv",
        "crypto",
        "ethereum",
        "generic",
        "hedera",
        "ripple",
        "tezos",
        "support",
        "version"
).map { "$corePath/src/$it" }

val cppCryptoIncDirs = cppCryptoSrcDirs + listOf(
        "src",
        "src/support",
        "include",
        "vendor",
        "vendor/secp256k1",
        "vendor/ed25519",
        "vendor/sqlite3"
).map { "$corePath/$it" }

if (hasAndroid) {
    apply(plugin = "com.android.library")
    configure<LibraryExtension> {
        compileSdkVersion(28)
        defaultConfig {
            minSdkVersion(23)
            targetSdkVersion(28)
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
    }
}

val publicApiHeaders = (file("$corePath/include").listFiles() ?: emptyArray())
        .filter { it.name.endsWith(".h") }
val privateApiHeaders = (file("$corePath/src/crypto").listFiles() ?: emptyArray())
        .filter { it.name.endsWith(".h") }

kotlin {
    ios()
    jvm {
        withJava()
    }
    if (hasAndroid) {
        android { publishAllLibraryVariants() }
    }

    // TODO: Add other native targets
    val nativeTargets = listOf(
            macosX64("macos"),
            iosX64(),
            iosArm64(),
    )

    configure(nativeTargets) {
        // TODO: Resolve linking paths from native build plugin
        //val coreStaticPath = file("$coreLibPath/debug/static/$name").absolutePath
        //val sqliteStaticPath = file("$sqliteLibPath/debug/static/$name").absolutePath
        //val ed25519StaticPath = file("$ed25519LibPath/debug/static/$name").absolutePath
        //val blake2StaticPath = file("$blake2LibPath/debug/static/$name").absolutePath
        val coreStaticPath = file("build/libs/corecrypto/static/$name").absolutePath
        val sqliteStaticPath = file("build/libs/sqlite3/static/$name").absolutePath
        val ed25519StaticPath = file("build/libs/ed25519/static/$name").absolutePath
        val blake2StaticPath = file("build/libs/blake2/static/$name").absolutePath
        binaries {
            getTest(DEBUG).linkerOpts(
                    "-L$coreStaticPath",
                    "-L$sqliteStaticPath",
                    "-L$ed25519StaticPath",
                    "-L$blake2StaticPath",
                    "-framework", "Security"
            )
        }

        if (name.startsWith("ios")) {
            // Create .frameworks for use in Obj-c/Swift projects
            binaries.framework {
                linkerOpts(
                        "-L$coreStaticPath",
                        "-L$sqliteStaticPath",
                        "-L$ed25519StaticPath",
                        "-L$blake2StaticPath",
                        "-framework", "Security"
                )
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
                        "-include-binary", "$coreStaticPath/libcorecrypto.a",
                        "-include-binary", "$sqliteStaticPath/libsqlite3.a",
                        "-include-binary", "$ed25519StaticPath/libed25519.a",
                        "-include-binary", "$blake2StaticPath/libblake2.a",
                        //"-memory-model", "experimental",
                        "-Xallocator=mimalloc"
                )
            }
            val BRCrypto by cinterops.creating {
                packageName = "brcrypto"
                headers(publicApiHeaders)
                headers(privateApiHeaders)
                includeDirs(cppCryptoSrcDir, cppCryptoIncludeDir)
            }
        }
    }

    sourceSets {
        all {
            languageSettings.apply {
                enableLanguageFeature("InlineClasses")
                useExperimentalAnnotation("kotlin.RequiresOptIn")
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
                implementation("org.jetbrains.kotlin:kotlin-test-common:$KOTLIN_VERSION")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common:$KOTLIN_VERSION")
            }
        }

        val jvmCommonMain by creating {
            targetFromPreset(presets["jvm"])
            dependsOn(commonMain)
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:$KTOR_VERSION")

                if (ideaActive) {
                    implementation("com.breadwallet.core:corenative-jre")
                }

                // TODO(fix): Guava is missing from the published corenative-jre pom
                implementation("com.google.guava:guava:28.1-jre")
            }
        }

        val jvmCommonTest by creating {
            dependsOn(commonTest)
        }

        val jvmMain by getting {
            dependsOn(jvmCommonMain)
            dependencies {
                implementation("com.breadwallet.core:corenative-jre")
            }
        }

        val jvmTest by getting {
            dependsOn(jvmCommonTest)
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test:$KOTLIN_VERSION")
                implementation("org.jetbrains.kotlin:kotlin-test-junit:$KOTLIN_VERSION")
            }
        }

        if (hasAndroid) {
            named("androidMain") {
                dependsOn(jvmCommonMain)
                dependencies {
                    implementation("com.breadwallet.core:corenative-android")
                }
            }

            named("androidTest") {
                dependsOn(jvmCommonTest)
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

apply(from = "../gradle/native-model.gradle")

dependencies {
    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:$DOKKA_VERSION")
}
