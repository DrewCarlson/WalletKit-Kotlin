import org.jetbrains.compose.compose
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose") version "0.3.0-build139"
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
        binaries {
            framework {
                baseName = "DemoWalletKotlin"
                export(rootProject)
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
                implementation("io.ktor:ktor-client-core:$KTOR_VERSION")
                implementation("drewcarlson.coingecko:coingecko:$COINGECKO_VERSION")
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

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        useIR = true
    }
}

application {
    mainClassName = "demo.MainKt"
}
