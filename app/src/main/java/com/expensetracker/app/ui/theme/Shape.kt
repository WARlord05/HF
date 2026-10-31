package com.expensetracker.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Pocket Ledger Shapes:
 * - 999dp for chips and badges
 * - 12dp for cards, inputs, and dialogs / bottom sheets
 * - 0dp for list rows (flat rows separated by hairlines)
 */
val LedgerShapes = Shapes(
    small = RoundedCornerShape(999.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(12.dp)
)

object LedgerDimensions {
    val hairline = 1.dp
    val monogramSize = 40.dp
    val rowHeight = 64.dp
    val horizontalGutter = 20.dp
    val verticalItemSpacing = 12.dp
}
