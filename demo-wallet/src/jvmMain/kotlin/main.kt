package demo

import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import drewcarlson.walletkit.Transfer
import drewcarlson.walletkit.TransferDirection
import drewcarlson.walletkit.WalletEvent
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
        DemoApplication().start { system, manager, wallet, event ->
            if (event is WalletEvent.TransferAdded) {
                transfers.add(event.transfer)
            }
        }
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
