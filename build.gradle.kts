import com.android.build.gradle.LibraryExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:$ATOMICFU_VERSION")
        classpath("com.android.tools.build:gradle:$AGP_VERSION")
    }
    repositories {
        mavenCentral()
        jcenter()
        google()
        maven { setUrl("https://kotlin.bintray.com/kotlinx") }
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
    }
}

plugins {
    kotlin("multiplatform") version KOTLIN_VERSION
    kotlin("plugin.serialization") version KOTLIN_VERSION
    id("org.jetbrains.dokka") version DOKKA_VERSION
    `maven-publish`
}

allprojects {
    repositories {
        maven { setUrl("https://dl.bintray.com/drewcarlson/Blockset-Kotlin") }
        mavenCentral()
        google()
        jcenter()
        maven { setUrl("https://kotlin.bintray.com/kotlinx") }
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
        maven { setUrl("https://dl.bintray.com/brd/walletkit-java") }
    }
}

val ideaActive = System.getProperty("idea.active") == "true"
val hasAndroid = false// TODO: fixme System.getProperty("sdk.dir") != null || System.getenv("ANDROID_HOME") != null

apply(plugin = "kotlinx-atomicfu")
apply(plugin = "idea")

val corePath = file("walletkit/WalletKitCore").absolutePath
val coreLibPath = file("libs/corecrypto/build/lib/main").absolutePath
val sqliteLibPath = file("libs/sqlite3/build/lib/main").absolutePath
val ed25519LibPath = file("libs/ed25519/build/lib/main").absolutePath
val blake2LibPath = file("libs/blake2/build/lib/main").absolutePath

val cppCryptoSrcDir = "$corePath/src"
val cppCryptoIncludeDir = "$corePath/include"
val cppCryptoSrcDirs = listOf(
        "bcash",
        "bitcoin",
        "crypto",
        "ethereum",
        "generic",
        "hedera",
        "ripple",
        "bsv",
        "tezos",
        "support"
).map { "$corePath/src/$it" }

val cppCryptoIncDirs = cppCryptoSrcDirs + listOf(
        "src",
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

val wkdir = file("walletkit")
if (!wkdir.exists() || wkdir.listFiles()?.isNullOrEmpty() == true) {
    exec {
        commandLine("git", "submodule", "update", "--init", "--recursive")
    }
}

val publicApiHeaders = (file("$corePath/include").listFiles() ?: emptyArray())
        .filter { it.name.endsWith(".h") }
val privateApiHeaders = (file("$corePath/src/crypto").listFiles() ?: emptyArray())
        .filter { it.name.endsWith(".h") }

System.getenv("GITHUB_REF")?.let { ref ->
    if (ref.startsWith("refs/tags/")) {
        version = ref.substringAfterLast("refs/tags/")
    }
}

val mavenUrl: String by ext
val mavenSnapshotUrl: String by ext

publishing {
    repositories {
        maven {
            url = if (version.toString().endsWith("SNAPSHOT")) {
                uri(mavenSnapshotUrl)
            } else {
                uri(mavenUrl)
            }
            credentials {
                username = System.getenv("BINTRAY_USER")
                password = System.getenv("BINTRAY_API_KEY")
            }
        }
    }
}

kotlin {
    ios()
    jvm()
    if (hasAndroid) {
        android { publishAllLibraryVariants() }
    }

    // TODO: Add other native targets
    val nativeTargets = listOf(
            macosX64("macos"),
            iosX64(),
            iosArm64()
    )

    // TODO: Adapt native configuration to support windows, linux, etc
    configure(nativeTargets) {
        // TODO: Resolve linking paths from native build plugin
        //val coreStaticPath = file("$coreLibPath/debug/static/$name").absolutePath
        //val sqliteStaticPath = file("$sqliteLibPath/debug/static/$name").absolutePath
        //val ed25519StaticPath = file("$ed25519LibPath/debug/static/$name").absolutePath
        //val blake2StaticPath = file("$blake2LibPath/debug/static/$name").absolutePath
        val coreStaticPath = file("./build/libs/corecrypto/static/$name").absolutePath
        val sqliteStaticPath = file("./build/libs/sqlite3/static/$name").absolutePath
        val ed25519StaticPath = file("./build/libs/ed25519/static/$name").absolutePath
        val blake2StaticPath = file("./build/libs/blake2/static/$name").absolutePath
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
                useExperimentalAnnotation("kotlin.ExperimentalUnsignedTypes")
                useExperimentalAnnotation("kotlin.RequiresOptIn")
                progressiveMode = true
                explicitApi()
            }
        }

        val commonMain by getting {
            dependencies {
                api("drewcarlson.blockset:blockset:$BLOCKSET_VERSION")
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
            dependsOn(commonMain)
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib:$KOTLIN_VERSION")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$COROUTINES_VERSION")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$SERIALIZATION_VERSION")

                implementation("io.ktor:ktor-client-okhttp:$KTOR_VERSION")
                implementation("io.ktor:ktor-client-json-jvm:$KTOR_VERSION")
                implementation("io.ktor:ktor-client-serialization-jvm:$KTOR_VERSION")

                if (ideaActive) {
                    implementation("com.breadwallet.core:corenative-jre:$CORENATIVE_VERSION")
                }

                // TODO(fix): Guava is missing from the published corenative-jre pom
                implementation("com.google.guava:guava:28.1-jre")
            }
        }

        val jvmMain by getting {
            dependsOn(jvmCommonMain)
            dependencies {
                implementation("com.breadwallet.core:corenative-jre:$CORENATIVE_VERSION")
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test:$KOTLIN_VERSION")
                implementation("org.jetbrains.kotlin:kotlin-test-junit:$KOTLIN_VERSION")
            }
        }

        if (hasAndroid) {
            val androidMain by getting {
                dependsOn(jvmCommonMain)
                dependencies {
                    implementation("com.breadwallet.core:corenative-android:$CORENATIVE_VERSION")
                }
            }

            val androidTest by getting {
                dependsOn(jvmTest)
            }
        }

        // TODO: HMPP structures cannot share cinterop dependencies in common sources
        //   this should be renamed to darwinMain and consumed by darwin targets when
        //   that restriction is removed.
        val darwinDependenciesMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$COROUTINES_VERSION")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$SERIALIZATION_VERSION")
                implementation("io.ktor:ktor-client-json:$KTOR_VERSION")
                implementation("io.ktor:ktor-client-serialization:$KTOR_VERSION")
                implementation("io.ktor:ktor-client-logging:$KTOR_VERSION")
                implementation("co.touchlab:stately-iso-collections:$STATELY_VERSION")
            }
        }

        val iosX64Main by getting {
            dependsOn(darwinDependenciesMain)
            kotlin.srcDirs("src/darwinMain")
            dependencies {
                implementation("io.ktor:ktor-client-ios:$KTOR_VERSION")
            }
        }

        val iosArm64Main by getting {
            dependsOn(darwinDependenciesMain)
            kotlin.srcDirs("src/darwinMain")
            dependencies {
                implementation("io.ktor:ktor-client-ios:$KTOR_VERSION")
            }
        }

        val macosMain by getting {
            dependsOn(darwinDependenciesMain)
            kotlin.srcDirs("src/darwinMain")
            dependencies {
                implementation("io.ktor:ktor-client-curl:$KTOR_VERSION")
            }
        }
    }
}

apply(from = "gradle/native-model.gradle")

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.useIR = true
}

tasks.create("patchDocs") {
    dependsOn(tasks.dokkaHtml)
    mustRunAfter(tasks.dokkaHtml)
    doLast {
        val outDir = tasks.dokkaHtml.get().outputDirectory.get()
        val innerDir = File(outDir, project.name)
        val moduleIndex = File(innerDir, "index.html").readText()
        val newIndex = File(outDir, "index.html")
        val packages = (innerDir.listFiles() ?: emptyArray()).toList()
        val stringReplace = packages.map { file ->
            { input: String ->
                input.replace(
                        "href=\"${file.name}",
                        "href=\"./${project.name}/${file.name}"
                )
            }
        }
        val patchedIndex = stringReplace
                .fold(moduleIndex) { acc, replace -> replace(acc) }
                .replace("\"../", "\"./")
        newIndex.writeText(patchedIndex)
    }
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTask> {
    //moduleName.set("WalletKit Kotlin")
}

dependencies {
    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:$DOKKA_VERSION")
}
