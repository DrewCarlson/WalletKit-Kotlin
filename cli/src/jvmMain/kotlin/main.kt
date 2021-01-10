package cli

import drewcarlson.blockset.BdbService
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.*
import kotlin.system.exitProcess

fun main(args: Array<String>): Unit = runBlocking {
    Thread.setDefaultUncaughtExceptionHandler { _, error ->
        println("Unhandled Exception: $error")
        error.printStackTrace()
        exitProcess(-1)
    }

    val bdbToken = checkNotNull(System.getenv("BDB_CLIENT_TOKEN"))
    runCli(args, bdbToken)
}

actual fun createBdbService(bdbToken: String): BdbService =
    BdbService.createForTest(bdbToken)

actual fun quit(): Nothing = exitProcess(0)

actual val uids: String = UUID.randomUUID().toString()

actual val storagePath: String by lazy {
    File(System.getProperty("user.home"), DATA_DIR_NAME).absolutePath
}

actual fun deleteData() {
    File(storagePath).delete()
}
