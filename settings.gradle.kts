pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
        maven { setUrl("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
}

rootProject.name = "walletkit"

include(":cli")
//include(":demo-wallet")
/*include(
    ":libs:blake2",
    ":libs:corecrypto",
    ":libs:ed25519",
    ":libs:sqlite3"
)*/

enableFeaturePreview("GRADLE_METADATA")
