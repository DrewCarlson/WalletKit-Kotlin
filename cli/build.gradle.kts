import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm {
        withJava()
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        val jvmJar by tasks.getting(Jar::class) {
            doFirst {
                archiveFileName.set("cli-jvm.jar")
                manifest {
                    attributes["Main-Class"] = "cli.MainKt"
                }
                from(
                    configurations.getByName("runtimeClasspath")
                        .map { if (it.isDirectory) it else zipTree(it) }
                )
                exclude("META-INF/versions/9/module-info.class")
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
                enableLanguageFeature("InlineClasses")
            }
        }
        named("commonMain") {
            dependencies {
                implementation(project(":library"))
                implementation("org.jetbrains.kotlinx:kotlinx-cli:$KOTLIN_CLI_VERSION")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$COROUTINES_VERSION")
                implementation("io.ktor:ktor-client-core:$KTOR_VERSION")
                implementation("io.ktor:ktor-client-json:$KTOR_VERSION")
                implementation("io.ktor:ktor-client-serialization:$KTOR_VERSION")
            }
        }

        named("macosMain") {
            dependencies {
                implementation("io.ktor:ktor-client-curl:$KTOR_VERSION")
            }
        }

        named("jvmMain") {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:$KTOR_VERSION")
            }
        }
    }
}

tasks.create("runJar", Exec::class) {
    group = "run"
    description = "Executes Kotlin/JVM jar"
    dependsOn(tasks.findByPath("jvmJar"))
    commandLine("java", "-jar", "./build/libs/cli-jvm.jar", "--help")
}
