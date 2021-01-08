import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.register
import org.gradle.model.Mutate
import org.gradle.model.RuleSource
import org.gradle.nativeplatform.toolchain.Clang
import org.gradle.nativeplatform.toolchain.NativeToolChainRegistry
import org.gradle.nativeplatform.toolchain.internal.gcc.DefaultGccPlatformToolChain
import java.io.File

fun safeExec(vararg script: String): String {
    if (!File("/usr/bin/" + script[0]).exists()) {
        return ""
    }
    return Runtime.getRuntime().exec(script)
        .inputStream
        .use { it.bufferedReader().readText() }
        .trim()
}

val iphoneOsSdk by lazy { safeExec("xcrun", "--sdk", "iphoneos", "--show-sdk-path") }
val iphoneSimSdk by lazy { safeExec("xcrun", "--sdk", "iphonesimulator", "--show-sdk-path") }
val macosSdk by lazy { safeExec("xcrun", "--sdk", "macosx", "--show-sdk-path") }
val clangPath by lazy {
    val clangBin = safeExec("xcrun", "--sdk", "macosx", "--find", "clang")
    if (clangBin.isNotEmpty()) {
        clangBin.substring(0, clangBin.lastIndexOf("/"))
    } else {
        "/usr/bin"
    }
}

fun iosClangCompilerArgs(args: MutableList<String>, sdk: String, arch: String) {
    args.addAll(
        listOfNotNull(
            "-isysroot$sdk",
            "-arch$arch",
            "-std=c99",
            "-stdlib=libc++",
            "-fembed-bitcode",
            "-O0",
            "-F${sdk}/System/Library/Frameworks",
            "-I${sdk}/user/include",
            if (sdk == iphoneOsSdk || sdk == iphoneSimSdk) "-miphoneos-version-min=10.0" else null
        )
    )
}

open class ToolchainConfiguration : RuleSource() {
    @Mutate
    fun NativeToolChainRegistry.configureToolchains() {
        if (OperatingSystem.current().isMacOsX) {
            register<Clang>("clang-ios") {
                val iosTargets = listOf(
                    Triple("ios_x86-64", iphoneSimSdk, "x86_64"),
                    Triple("ios_arm64", iphoneOsSdk, "arm64")
                )
                iosTargets.forEach { (name, sdk, arch) ->
                    target(name) {
                        this as DefaultGccPlatformToolChain
                        symbolExtractor.executable = "clang"
                        stripper.executable = "clang"
                        path(clangPath)
                        cppCompiler.apply {
                            executable = "clang"
                            withArguments {
                                iosClangCompilerArgs(this, sdk, arch)
                            }
                        }
                        getcCompiler().apply {
                            executable = "clang"
                            withArguments {
                                iosClangCompilerArgs(this, sdk, arch)
                            }
                        }
                        assembler.executable = "clang"
                        linker.apply {
                            executable = "clang"
                            withArguments {
                                add("-v")
                                add("-isysroot${sdk}")
                                removeAll(filter { it.startsWith("-Wl,-soname,") })
                            }
                        }
                    }
                }
            }
        }
    }
}
