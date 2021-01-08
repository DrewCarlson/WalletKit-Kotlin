plugins {
    `cpp-library`
}

val corePath = rootProject.file("walletkit/WalletKitCore").absolutePath
val cppblake2SrcDir = "$corePath/vendor/blake2"

library {
    baseName.set("blake2")
    linkage.addAll(Linkage.STATIC, Linkage.SHARED)
    targetMachines.addAll(
        machines.linux.x86_64,
        machines.windows.x86_64,
        machines.macOS.x86_64,
        machines.os("ios").architecture("x86-64"),
        machines.os("ios").architecture("arm64")
    )
    binaries.configureEach {
        compileTask.get().apply {
            source.from(fileTree(cppblake2SrcDir) {
                include("*.c")
            })
            compilerArgs.addAll("-x", "c", "-std=c99")
        }
    }
}
