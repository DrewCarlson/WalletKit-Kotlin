package demo

import androidx.compose.desktop.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import drewcarlson.walletkit.*
import java.util.*

fun main() {
    Window(
            title = "Demo Wallet",
            centered = true,
            undecorated = false,
            size = IntSize(400, 600),
    ) { appBody() }
}


@Composable
fun DemoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
            content = content,
            colors = lightColors()
    )
}

@Composable
fun appBody() = DemoTheme {
    Text("Hello Bitcoin")
    val instanceId = remember { UUID.randomUUID().toString() }
    val transfers = remember { mutableStateListOf<Transfer>() }
    Scaffold { padding ->
        Column(
                modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(4.dp)
        ) {
            Text(
                    text = "Transfers",
                    modifier = Modifier
                            .padding(vertical = 4.dp)
            )
            TransferList(transfers)
        }
    }

    LaunchedEffect(instanceId) {
        DemoApplication().start(object : DefaultSystemListener() {
            override fun handleWalletEvent(system: System, manager: WalletManager, wallet: Wallet, event: WalletEvent) {
                if (event is WalletEvent.TransferAdded) {
                    transfers.add(event.transfer)
                }
            }
        })
    }
}

@Composable
private fun TransferList(transfers: List<Transfer>) {
    LazyColumn(
            modifier = Modifier
                    .fillMaxSize()
    ) {
        items(transfers) { transfer ->
            when (transfer.direction) {
                TransferDirection.RECEIVED -> {
                    Row {
                        Text("From: ")
                        Text(
                                text = "${transfer.source}",
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                modifier = Modifier
                                        .fillParentMaxWidth(),
                        )
                    }
                    Text(
                            text = "${transfer.amount}",
                            modifier = Modifier,
                            color = Color.Green,
                    )
                }
                TransferDirection.SENT -> {
                    Row {
                        Text("To: ")
                        Text(
                                text = "${transfer.target}",
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                modifier = Modifier
                                        .fillParentMaxWidth(),
                        )
                    }
                    Text(
                            text = "${transfer.amount}",
                            modifier = Modifier,
                            color = Color.Red,
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
