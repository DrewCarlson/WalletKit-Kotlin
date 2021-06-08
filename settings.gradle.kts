pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "WalletKit-Kotlin"

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
        substitute(module("com.breadwallet.core:corenative-android"))
                .with(project(":corenative-android"))
        substitute(module("com.breadwallet.core:corenative-jre"))
                .with(project(":corenative-jre"))
    }
}

