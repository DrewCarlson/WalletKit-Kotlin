apply(plugin = "maven-publish")
apply(plugin = "signing")
apply(plugin = "org.jetbrains.dokka")

System.getenv("GITHUB_REF")?.let { ref ->
    if (ref.startsWith("refs/tags/v")) {
        version = ref.substringAfterLast("refs/tags/v")
    }
}

val mavenUrl: String by extra
val mavenSnapshotUrl: String by extra
val signingKey: String? by project
val signingPassword: String? by project
val sonatypeUsername: String? by project
val sonatypePassword: String? by project


configure<PublishingExtension> {
    components.all {
        publications.withType<MavenPublication> {
            with(pom) {
                name.set(rootProject.name)
                url.set("https://github.com/DrewCarlson/WalletKit-Kotlin")
                description.set("Multiplatform Kotlin bindings for WalletKit.")
                scm {
                    url.set("https://github.com/DrewCarlson/WalletKit-Kotlin.git")
                }
                developers {
                    developer {
                        id.set("DrewCarlson")
                        name.set("Drew Carlson")
                    }
                }
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/mit-license.php")
                        distribution.set("repo")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            url = if (version.toString().endsWith("SNAPSHOT")) {
                uri(mavenSnapshotUrl)
            } else {
                uri(mavenUrl)
            }
            credentials {
                username = sonatypeUsername
                password = sonatypePassword
            }
        }
    }
}

configure<SigningExtension> {
    isRequired = !version.toString().endsWith("SNAPSHOT")
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign((extensions["publishing"] as PublishingExtension).publications)
}