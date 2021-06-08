import org.jetbrains.compose.compose
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose") version JB_COMPOSE_VERSION
    application
}

repositories {
    mavenCentral()
    maven { setUrl("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
}

val genSrcFile = file("build/generated/constants")

val installTestConfig by tasks.creating {
    val configFile = rootProject.file("${genSrcFile.absolutePath}/config.kt")
    onlyIf { !configFile.exists() }
    doFirst {
        genSrcFile.also { if (!it.exists()) it.mkdirs() }
        val bdbClientToken = System.getenv("BDB_CLIENT_TOKEN")
        if (!configFile.exists()) {
            checkNotNull(bdbClientToken) {
                "BDB_CLIENT_TOKEN must be set for tests to run."
            }
            configFile.writeText(buildString {
                appendln("package demo")
                appendln("const val BDB_CLIENT_TOKEN = \"$bdbClientToken\"")
            })
        }
    }
}

kotlin {
    jvm {
        withJava()
    }
    iosX64 {
        compilations.getByName("main") {
            kotlinOptions {
                freeCompilerArgs += listOf("-Xallocator=mimalloc")
            }
        }
        binaries {
            framework {
                baseName = "DemoWalletKotlin"
                export(rootProject)
                export("org.drewcarlson:blockset:$BLOCKSET_VERSION")
                isStatic = true
                linkerOpts.addAll(listOf("-framework", "Security"))
            }
        }
    }

    targets.forEach { target ->
        target.compilations.forEach { compilation ->
            compilation.compileKotlinTask.dependsOn(installTestConfig)
        }
    }

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir(genSrcFile)
            dependencies {
                api(rootProject)
                api("org.drewcarlson:blockset:$BLOCKSET_VERSION")
                implementation("io.ktor:ktor-client-core:$KTOR_VERSION")
                implementation("org.drewcarlson:coingecko:$COINGECKO_VERSION")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                //implementation(compose.materialIconsExtended)
                implementation("io.ktor:ktor-client-okhttp:$KTOR_VERSION")
            }
        }

        val iosX64Main by getting {
            dependencies {
                implementation("io.ktor:ktor-client-ios:$KTOR_VERSION")
            }
        }
    }
}

application {
    mainClassName = "demo.MainKt"
}
