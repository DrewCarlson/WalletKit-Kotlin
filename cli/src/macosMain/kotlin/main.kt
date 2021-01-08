package cli

import co.touchlab.stately.freeze
import drewcarlson.walletkit.api.BdbService
import drewcarlson.walletkit.api.DarwinBdbService
import kotlinx.cinterop.autoreleasepool
import platform.Foundation.*
import kotlin.system.exitProcess

fun main(args: Array<String>): Unit = autoreleasepool {
    setUnhandledExceptionHook({ error: Throwable ->
        println("Unhandled Exception: $error")
        error.printStackTrace()
        exitProcess(-1)
    }.freeze())
    //Platform.isMemoryLeakCheckerActive = false
    val bdbToken = checkNotNull(
        NSProcessInfo.processInfo
            .environment["BDB_CLIENT_TOKEN"]
            ?.toString()
    )
    runCli(args, bdbToken)
}

actual fun createBdbService(bdbToken: String): BdbService =
    DarwinBdbService.createForTest(bdbToken)

actual fun quit(): Nothing = exitProcess(0)

actual val uids: String = NSUUID().UUIDString

@SharedImmutable
actual val storagePath: String by lazy {
    checkNotNull(
        NSURL(
            fileURLWithPath = DATA_DIR_NAME,
            relativeToURL = NSFileManager.defaultManager.homeDirectoryForCurrentUser
        ).path
    )
}

actual fun deleteData() {
    NSFileManager.defaultManager.removeFileAtPath(storagePath, null)
}
