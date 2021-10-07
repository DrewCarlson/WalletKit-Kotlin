pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "WalletKit-Kotlin"

enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

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
