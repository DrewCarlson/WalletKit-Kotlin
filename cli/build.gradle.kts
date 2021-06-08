import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

repositories {
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
}

kotlin {
    jvm {
        withJava()
        val jvmJar by tasks.getting(Jar::class) {
            doFirst {
                manifest {
                    attributes["Main-Class"] = "cli.MainKt"
                }
                from(
                    configurations.getByName("runtimeClasspath")
                        .map { if (it.isDirectory) it else zipTree(it) }
                )
            }
        }
    }
    macosX64("macos") {
        binaries {
            executable {
                entryPoint = "cli.main"
                linkerOpts.addAll(listOf("-framework", "Security"))
            }
        }

        compilations.getByName("main") {
            kotlinOptions {
                freeCompilerArgs += listOf("-Xallocator=mimalloc")
            }
        }
    }

    sourceSets {
        all {
            languageSettings.apply {
                useExperimentalAnnotation("kotlinx.cli.ExperimentalCli")
                useExperimentalAnnotation("kotlin.ExperimentalUnsignedTypes")
                enableLanguageFeature("InlineClasses")
            }
        }
        val commonMain by getting {
            dependencies {
                implementation(rootProject)
                implementation("org.jetbrains.kotlinx:kotlinx-cli:$KOTLIN_CLI_VERSION")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$COROUTINES_VERSION")
                implementation("io.ktor:ktor-client-core:$KTOR_VERSION")
                implementation("io.ktor:ktor-client-json:$KTOR_VERSION")
                implementation("io.ktor:ktor-client-serialization:$KTOR_VERSION")
            }
        }

        val macosMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-curl:$KTOR_VERSION")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$KOTLIN_VERSION")
                implementation("io.ktor:ktor-client-okhttp:$KTOR_VERSION")
            }
        }
    }
}

tasks.withType(KotlinCompile::class) {
    kotlinOptions {
        jvmTarget = "1.8"
        useIR = true
    }
}

tasks.create("runJar", Exec::class) {
    group = "run"
    description = "Executes Kotlin/JVM jar"
    dependsOn(tasks.findByPath("jvmJar"))
    commandLine("java", "-jar", "./build/libs/cli-jvm.jar", "--help")
}
