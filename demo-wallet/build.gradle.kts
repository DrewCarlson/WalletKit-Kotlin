import org.jetbrains.compose.compose

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
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
                appendLine("package demo")
                appendLine("const val BDB_CLIENT_TOKEN = \"$bdbClientToken\"")
            })
        }
    }
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }
    ios {
        compilations.getByName("main") {
            kotlinOptions {
                freeCompilerArgs += listOf("-Xallocator=mimalloc")
            }
        }
        binaries {
            framework {
                baseName = "DemoWalletKotlin"
                export(project(":library"))
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
        named("commonMain") {
            kotlin.srcDir(genSrcFile)
            dependencies {
                api(project(":library"))
                api("org.drewcarlson:blockset:$BLOCKSET_VERSION")
                implementation("io.ktor:ktor-client-core:$KTOR_VERSION")
            }
        }

        named("jvmMain") {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("io.ktor:ktor-client-okhttp:$KTOR_VERSION")
            }
        }

        named("iosMain") {
            dependencies {
                implementation("io.ktor:ktor-client-ios:$KTOR_VERSION")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "demo.MainKt"
    }
}
