plugins {
    kotlin("multiplatform") version KOTLIN_VERSION apply false
    kotlin("plugin.serialization") version KOTLIN_VERSION apply false
    id("org.jetbrains.dokka") version DOKKA_VERSION apply false
    id("org.jetbrains.compose") version JB_COMPOSE_VERSION apply false
}

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:$ATOMICFU_VERSION")
        classpath("com.android.tools.build:gradle:$AGP_VERSION")
    }
    repositories {
        mavenCentral()
        jcenter()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev/")
    }
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        jcenter()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev/")
    }
}

val wkdir = file("walletkit")
if (!wkdir.exists() || wkdir.listFiles()?.isNullOrEmpty() == true) {
    exec {
        commandLine("git", "submodule", "update", "--init", "--recursive")
    }
}
