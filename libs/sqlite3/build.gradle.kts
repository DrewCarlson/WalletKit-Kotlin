plugins {
    `cpp-library`
}

val corePath = rootProject.file("walletkit/WalletKitCore").absolutePath
val cppSqliteSrcDir = "$corePath/vendor/sqlite3"

library {
    baseName.set("sqlite3")
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
            source.from(fileTree(cppSqliteSrcDir) {
                include("*.c")
            })
            compilerArgs.addAll("-x", "c", "-std=c99")
        }
/*
    binaries
        .filterIsInstance<NativeLibraryBinarySpec>()
        .forEach { spec ->
            // TODO(fix): Do we want to use this approach (config.h)?
            spec.getcCompiler().define("_HAVE_SQLITE_CONFIG_H")
        }
    binaries.withType(StaticLibraryBinarySpec::class) {
        //buildable = false
    }
*/
    }
}
