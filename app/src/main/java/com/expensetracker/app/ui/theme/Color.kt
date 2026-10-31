package com.expensetracker.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Pocket Ledger Colour System as specified in DESIGN.md.
 * One accent (deep green), a warm neutral scale (warm paper / ink), and semantic colours used sparingly.
 */

// Light theme ("Paper")
val PaperBackground = Color(0xFFF6F2EA)
val PaperSurface = Color(0xFFFBF8F2)
val PaperSurfaceVariant = Color(0xFFEDE7DA)
val PaperOutline = Color(0xFFD9D1C1)
val PaperOnBackground = Color(0xFF1F1B16)
val PaperOnSurfaceMuted = Color(0xFF6B6358)
val PaperAccent = Color(0xFF2F5D50)
val PaperOnAccent = Color(0xFFFBF8F2)
val PaperDebit = Color(0xFF1F1B16)
val PaperCredit = Color(0xFF2F7A4F)
val PaperWarning = Color(0xFFB7791F)
val PaperDanger = Color(0xFFA63D2F)

// Dark theme ("Ink")
val InkBackground = Color(0xFF141311)
val InkSurface = Color(0xFF1C1A17)
val InkSurfaceVariant = Color(0xFF27241F)
val InkOutline = Color(0xFF3A362F)
val InkOnBackground = Color(0xFFEDE8DD)
val InkOnSurfaceMuted = Color(0xFF9A9284)
val InkAccent = Color(0xFF7FB5A3)
val InkOnAccent = Color(0xFF10201B)
val InkCredit = Color(0xFF6FBF8E)
val InkWarning = Color(0xFFD9A441)
val InkDanger = Color(0xFFD9786A)

// Category tints: low chroma, muted tint on monogram circle
object CategoryColors {
    val Food = Color(0xFFC98B5A)
    val Shopping = Color(0xFF8A7BA8)
    val Travel = Color(0xFF5E8FA8)
    val Utility = Color(0xFFB39B4D)
    val Subscriptions = Color(0xFF2F5D50)
    val Other = Color(0xFF8C867B)
    val Health = Color(0xFF4E8775)
    val Entertainment = Color(0xFF7A6B8A)

    fun colorFor(category: String): Color = when (category.trim().lowercase()) {
        "food", "dining", "groceries" -> Food
        "shopping", "ecommerce" -> Shopping
        "travel", "commute", "fuel", "cab" -> Travel
        "utility", "bills", "electricity", "recharge" -> Utility
        "subscription", "subscriptions", "autopay" -> Subscriptions
        "entertainment", "movies", "ott" -> Entertainment
        "health", "medical", "pharmacy" -> Health
        else -> Other
    }
}

@Immutable
data class LedgerExtras(
    val credit: Color,
    val debit: Color,
    val warning: Color,
    val danger: Color,
    val mutedText: Color,
    val hairline: Color
)

val LocalLedgerExtras = staticCompositionLocalOf {
    LedgerExtras(
        credit = PaperCredit,
        debit = PaperDebit,
        warning = PaperWarning,
        danger = PaperDanger,
        mutedText = PaperOnSurfaceMuted,
        hairline = PaperOutline
    )
}
