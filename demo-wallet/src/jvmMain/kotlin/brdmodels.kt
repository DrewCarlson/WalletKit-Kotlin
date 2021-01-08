package demo

import drewcarlson.walletkit.Amount
import drewcarlson.walletkit.CUnit
import drewcarlson.walletkit.Wallet
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.math.absoluteValue

// TODO: Move somewhere UI related
fun BigDecimal.formatCryptoForUi(
    currencyCode: String,
    scale: Int = 5,
    negate: Boolean = false
): String {
    val amount = if (negate) negate() else this

    val currencyFormat = DecimalFormat.getCurrencyInstance(Locale.getDefault()) as DecimalFormat
    val decimalFormatSymbols = currencyFormat.decimalFormatSymbols
    currencyFormat.isGroupingUsed = true
    currencyFormat.roundingMode = RoundingMode.HALF_EVEN
    decimalFormatSymbols.currencySymbol = ""
    currencyFormat.decimalFormatSymbols = decimalFormatSymbols
    currencyFormat.maximumFractionDigits = scale
    currencyFormat.minimumFractionDigits = 0
    return "${currencyFormat.format(amount)} ${currencyCode.toUpperCase()}"
}

// TODO: Move somewhere UI related
fun BigDecimal.formatFiatForUi(currencyCode: String, scale: Int? = null): String {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault()) as DecimalFormat
    val decimalFormatSymbols = currencyFormat.decimalFormatSymbols
    currencyFormat.isGroupingUsed = true
    currencyFormat.roundingMode = RoundingMode.HALF_EVEN
    // TODO: Current Security Model does not allow access to system variables in preview
    //val currency = java.util.Currency.getInstance(currencyCode)
    //val symbol = currency.symbol
    decimalFormatSymbols.currencySymbol = "$"
    currencyFormat.decimalFormatSymbols = decimalFormatSymbols
    currencyFormat.negativePrefix = "-$"
    currencyFormat.maximumFractionDigits = scale ?: 2 //currency.defaultFractionDigits
    currencyFormat.minimumFractionDigits = scale ?: 2  //currency.defaultFractionDigits

    return currencyFormat.format(this)
}

/**
 * Price change of a currency over the last 24Hrs.
 */
data class PriceChange(
    val changePercentage24Hrs: Double,
    val change24Hrs: Double
) {

    private val arrow: String = when {
        change24Hrs > 0 -> "\u25B4"
        change24Hrs < 0 -> "\u25BE"
        else -> ""
    }

    override fun toString(): String {
        val amount = String.format(Locale.getDefault(), "%.2f", change24Hrs.absoluteValue)
        val percentage =
            String.format(Locale.getDefault(), "%.2f", changePercentage24Hrs.absoluteValue)
        return "$arrow $percentage% ($amount)"
    }

    fun getPercentageChange(): String {
        val percentage =
            String.format(Locale.getDefault(), "%.2f", changePercentage24Hrs.absoluteValue)
        return "$arrow $percentage%"
    }
}


enum class PromptItem {
    EMAIL_COLLECTION,
    FINGER_PRINT,
    PAPER_KEY,
    UPGRADE_PIN,
    RECOMMEND_RESCAN,
    RATE_APP
}

data class M(
    val wallets: Map<String, UIWallet> = emptyMap(),
    val displayOrder: List<String> = emptyList(),
    val promptId: PromptItem? = null,
    val hasInternet: Boolean = true,
    val isBuyBellNeeded: Boolean = false,
    val showBuyAndSell: Boolean = false,
    val rateAppPromptDontShowMeAgain: Boolean = false
) {

    companion object {
        @JvmStatic
        fun createDefault() = M()
    }

    val aggregatedFiatBalance: BigDecimal = wallets.values
        .fold(BigDecimal.ZERO) { acc, next ->
            acc.add(next.fiatBalance)
        }

    val showPrompt: Boolean = promptId != null
}


data class UIWallet(
    val currencyId: String,
    val currencyName: String,
    val currencyCode: String,
    val fiatPricePerUnit: BigDecimal = BigDecimal.ZERO,
    val balance: BigDecimal = BigDecimal.ZERO,
    val fiatBalance: BigDecimal = BigDecimal.ZERO,
    val syncProgress: Float = 0f,
    val syncingThroughMillis: Long = 0L,
    val isSyncing: Boolean = false,
    val priceChange: PriceChange? = null,
    val state: State = State.READY,
    val startColor: String? = null,
    val endColor: String? = null,
    val isSupported: Boolean = true
) {
    enum class State {
        READY, LOADING, UNINITIALIZED
    }

    val hasSyncTime: Boolean = syncingThroughMillis != 0L

    val hasPricePerUnit: Boolean = fiatPricePerUnit != BigDecimal.ZERO
}


fun Wallet.asUIWallet(): UIWallet {
    //val tokenItem = TokenUtil.tokenForCode(currency.code)
    val balanceBig = balance.toBigDecimal()
    return UIWallet(
        currencyId = "",
        currencyName = currency.name,
        currencyCode = currency.code,
        fiatPricePerUnit = BigDecimal.ZERO,
        balance = balanceBig,
        fiatBalance = BigDecimal.ZERO,
        syncProgress = 0f, // will update via sync events
        syncingThroughMillis = 0L, // will update via sync events
        priceChange = null,
        state = UIWallet.State.READY,
        isSyncing = false,
        startColor = "",
        endColor = "",
        isSupported = true
    )
}

fun Amount.toBigDecimal(
    unit: CUnit = this.unit,
    roundingMode: RoundingMode = RoundingMode.HALF_EVEN
): BigDecimal {
    return BigDecimal(asDouble(unit) ?: 0.0)
        .setScale(unit.decimals.toInt(), roundingMode)
}
