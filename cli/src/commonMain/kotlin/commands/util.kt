package cli.commands

fun printlnGreen(message: Any) = println("\u001b[32m$message\u001b[0m")
fun printlnMagenta(message: Any) = println("\u001b[35m$message\u001b[0m")