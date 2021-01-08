package demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawOpacity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.HorizontalGradient
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import drewcarlson.walletkit.System
import drewcarlson.walletkit.Wallet
import drewcarlson.walletkit.WalletEvent
import drewcarlson.walletkit.WalletListener
import drewcarlson.walletkit.WalletManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.math.BigDecimal

object BrdColors {
    val error = Color(0xFFEA5A5A)
    val background = Color(0xFF251935)
    val surface = Color(0xFF302442)
}

@Composable
fun BrdTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        content = content,
        colors = darkColors(
            error = BrdColors.error,
            background = BrdColors.background,
            surface = BrdColors.surface
        )
    )
}

@Composable
fun appBody() = BrdTheme {
    Text("Hello Bitcoin")
    val wallets = remember { mutableStateListOf<UIWallet>() }
    Scaffold(
        bottomBar = { MainNavigationBar() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(it)
        ) {
            DebugLabel()
            WalletSummary(BigDecimal.ZERO)
            WalletList(wallets)
        }
    }


    GlobalScope.launch {
        DemoApplication().start { system, manager, wallet, event ->
            wallets.clear()
            wallets.addAll(system.wallets.map { it.asUIWallet() })
        }
    }
}

@Composable
private fun WalletList(wallets: SnapshotStateList<UIWallet>) {
    LazyColumnFor(
        items = wallets,
        modifier = Modifier
            .fillMaxWidth()
    ) { wallet ->
        HomeScreenWalletItem(wallet = wallet)
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun MainNavigationBar() {
    BottomAppBar(
        elevation = 0.dp,
        backgroundColor = BrdColors.surface
    ) {
        BottomNavigationItem(
            icon = {
                //Image(asset = vectorResource(id = R.drawable.ic_menu_buy_sell))
            },
            label = { Text(text = "Buy") },
            selected = false,
            onClick = {}
        )
        BottomNavigationItem(
            icon = {
                //Image(asset = vectorResource(id = R.drawable.ic_menu_trade))
            },
            label = { Text(text = "Trade") },
            selected = false,
            onClick = {}
        )
        BottomNavigationItem(
            icon = {
                //Image(asset = vectorResource(id = R.drawable.ic_menu_more))
            },
            label = { Text(text = "Menu") },
            selected = false,
            onClick = {}
        )
    }
}

@Composable
private fun DebugLabel(isDebug: Boolean = true) {
    if (isDebug) {
        Text(
            text = "TESTNET 4.6.0 build 1",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 4.dp, bottom = 4.dp)
                .alpha(.5f)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun WalletSummary(fiatBalance: BigDecimal) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                text = "Total Assets",
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .alpha(.75f)
            )
            Text(
                fontWeight = FontWeight.Bold,
                text = fiatBalance.formatFiatForUi("USD")
            )
        }
    }
}

@Composable
private fun HomeScreenWalletItem(wallet: UIWallet) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(
                shape = RoundedCornerShape(4.dp),
                brush = HorizontalGradient(
                    startX = 0f,
                    endX = 1200f, // TODO: get row end
                    colors = listOf(
                        Color(0xFFFF9900),
                        Color(0xFFFF9900)
                    )
                )
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            /*CurrencyIcon(
                currencyCode = wallet.currencyCode
            )*/
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                CurrencyDetails(wallet = wallet)
                WalletDetails(wallet = wallet)
            }
        }
    }
}

@Composable
private fun CurrencyDetails(wallet: UIWallet) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Text(
            text = wallet.currencyName,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .drawOpacity(.75f)
        ) {
            Text(
                text = wallet.fiatPricePerUnit.formatFiatForUi("USD"),
                fontWeight = FontWeight.Bold
            )
            wallet.priceChange?.run {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = getPercentageChange(),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun WalletDetails(wallet: UIWallet) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Text(
            text = wallet.fiatBalance.formatFiatForUi("USD"),
            fontWeight = FontWeight.Bold
        )
        if (wallet.isSyncing) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Syncing",
                    fontWeight = FontWeight.Bold
                )
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 1.dp,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(10.dp)
                )
            }
        } else {
            Text(
                text = wallet.balance.formatCryptoForUi(wallet.currencyCode),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

