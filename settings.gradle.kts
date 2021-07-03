pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
    }
}

rootProject.name = "WalletKit-Kotlin"

include(":library")
include(":cli")
include(":demo-wallet")
/*include(
    ":libs:blake2",
    ":libs:corecrypto",
    ":libs:ed25519",
    ":libs:sqlite3"
)*/

includeBuild("walletkit/WalletKitJava") {
    dependencySubstitution {
//        substitute(module("com.breadwallet.core:corenative-android"))
//            .using(project(":corenative-android"))
        substitute(module("com.breadwallet.core:corenative-jre"))
            .using(project(":corenative-jre"))
    }
}

