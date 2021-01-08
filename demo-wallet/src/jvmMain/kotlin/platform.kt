package demo

import java.io.File
import java.util.UUID
import kotlin.system.exitProcess

actual fun quit(): Nothing = exitProcess(0)

actual val uids: String = UUID.randomUUID().toString()

actual val storagePath: String by lazy {
    File(System.getProperty("user.home"), "demo-wallet").absolutePath
}

actual fun deleteData() {
    File(storagePath).delete()
}
