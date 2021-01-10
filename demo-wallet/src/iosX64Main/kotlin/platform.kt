package demo

import platform.Foundation.*
import kotlin.system.exitProcess

actual fun quit(): Nothing = exitProcess(0)

actual val uids: String = NSUUID().UUIDString

@SharedImmutable
actual val storagePath: String by lazy {
    checkNotNull(
        NSURL(
            fileURLWithPath = "walletkit-cli",
            relativeToURL = NSFileManager.defaultManager.temporaryDirectory
        ).path
    )
}

actual fun deleteData() {
    NSFileManager.defaultManager.removeItemAtPath(storagePath, null)
}
