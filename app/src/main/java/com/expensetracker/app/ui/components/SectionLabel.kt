package com.expensetracker.app.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.expensetracker.app.ui.theme.LocalLedgerExtras
import com.expensetracker.app.ui.theme.SectionLabelStyle
import java.util.Locale

/**
 * Uppercase, tracked section header (+8% tracking, muted ink).
 */
@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color? = null
) {
    val textColor = color ?: LocalLedgerExtras.current.mutedText
    Text(
        text = text.uppercase(Locale.getDefault()),
        style = SectionLabelStyle,
        color = textColor,
        modifier = modifier
    )
}
