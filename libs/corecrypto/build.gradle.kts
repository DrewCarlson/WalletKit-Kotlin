plugins {
    `cpp-library`
}

val corePath = rootProject.file("walletkit/WalletKitCore").absolutePath

val cppCryptoSrcDir = "$corePath/src"
val cppCryptoSrcDirs = listOf(
    "bcash", "bitcoin", "crypto", "ethereum", "generic",
    "hedera", "ripple", "bsv", "tezos", "support"
).map { "$corePath/src/$it" }

val cppCryptoIncDirs = cppCryptoSrcDirs + listOf(
    "src",
    "include",
    "vendor",
    "vendor/secp256k1",
    "vendor/ed25519",
    "vendor/sqlite3"
).map { "$corePath/$it" }

library {
    baseName.set("corecrypto")
    linkage.addAll(Linkage.STATIC, Linkage.SHARED)
    targetMachines.addAll(
        machines.linux.x86_64,
        machines.windows.x86_64,
        machines.macOS.x86_64,
        machines.os("ios").architecture("x86-64"),
        machines.os("ios").architecture("arm64")
    )
    dependencies {
        implementation(project(":libs:blake2"))
        implementation(project(":libs:ed25519"))
        implementation(project(":libs:sqlite3"))
    }
    binaries.configureEach {
        compileTask.get().apply {
            source.from(fileTree(cppCryptoSrcDir) {
                include("**/*.c")
            })

            cppCryptoIncDirs.forEach {
                compilerArgs.add("-I$it")
            }
            compilerArgs.addAll("-x", "c", "-std=c99")
        }
    }
}

afterEvaluate {
    tasks.withType<LinkSharedLibrary> {
        val os = targetPlatform.get().operatingSystem
        if (os.isMacOsX || os.name.contains("ios", true)) {
            linkerArgs.addAll("-framework", "Security")
            linkerArgs.add("-lresolv")
        }
    }
}

