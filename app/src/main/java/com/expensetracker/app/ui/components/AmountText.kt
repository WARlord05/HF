package com.expensetracker.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.expensetracker.app.ui.formatMoney
import com.expensetracker.app.ui.theme.LocalLedgerExtras
import com.expensetracker.app.ui.theme.RowAmountStyle

/**
 * AmountText displays financial numbers with tabular figures (tnum) so digits align.
 * Uses a true minus '−' for debit and '+' for credit.
 * Debits use neutral ink (onBackground) per DESIGN.md to avoid unnecessary alarm.
 */
@Composable
fun AmountText(
    amount: Double,
    isCredit: Boolean,
    currency: String,
    modifier: Modifier = Modifier,
    showSign: Boolean = true,
    style: TextStyle = RowAmountStyle,
    color: Color? = null,
    textAlign: TextAlign = TextAlign.End
) {
    val extras = LocalLedgerExtras.current
    val sign = when {
        !showSign -> ""
        isCredit -> "+"
        else -> "−" // True Unicode minus sign (U+2212)
    }

    val textColor = color ?: if (isCredit) extras.credit else MaterialTheme.colorScheme.onBackground
    val formattedMoney = formatMoney(amount, currency)

    Text(
        text = "$sign$formattedMoney",
        color = textColor,
        style = style.copy(fontFeatureSettings = "tnum"),
        textAlign = textAlign,
        modifier = modifier
    )
}
