pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
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

project(":library").name = "walletkit"

includeBuild("walletkit/WalletKitJava") {
    dependencySubstitution {
        substitute(module("com.blockset.walletkit:WalletKitNative-JRE"))
                .using(project(":WalletKitNative-JRE"))
        substitute(module("com.blockset.walletkit:WalletKitNative-Android"))
                .using(project(":WalletKitNative-Android"))
    }
}

