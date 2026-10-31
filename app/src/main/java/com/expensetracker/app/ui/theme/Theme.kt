package com.expensetracker.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val PaperColors = lightColorScheme(
    primary = PaperAccent,
    onPrimary = PaperOnAccent,
    primaryContainer = PaperSurfaceVariant,
    onPrimaryContainer = PaperOnBackground,
    secondary = PaperAccent,
    onSecondary = PaperOnAccent,
    secondaryContainer = PaperSurfaceVariant,
    onSecondaryContainer = PaperOnBackground,
    tertiary = PaperAccent,
    onTertiary = PaperOnAccent,
    tertiaryContainer = PaperSurfaceVariant,
    onTertiaryContainer = PaperOnBackground,
    background = PaperBackground,
    onBackground = PaperOnBackground,
    surface = PaperSurface,
    onSurface = PaperOnBackground,
    surfaceVariant = PaperSurfaceVariant,
    onSurfaceVariant = PaperOnSurfaceMuted,
    outline = PaperOutline,
    outlineVariant = PaperOutline.copy(alpha = 0.5f),
    error = PaperDanger,
    onError = PaperOnAccent,
    errorContainer = PaperSurfaceVariant,
    onErrorContainer = PaperDanger
)

private val InkColors = darkColorScheme(
    primary = InkAccent,
    onPrimary = InkOnAccent,
    primaryContainer = InkSurfaceVariant,
    onPrimaryContainer = InkOnBackground,
    secondary = InkAccent,
    onSecondary = InkOnAccent,
    secondaryContainer = InkSurfaceVariant,
    onSecondaryContainer = InkOnBackground,
    tertiary = InkAccent,
    onTertiary = InkOnAccent,
    tertiaryContainer = InkSurfaceVariant,
    onTertiaryContainer = InkOnBackground,
    background = InkBackground,
    onBackground = InkOnBackground,
    surface = InkSurface,
    onSurface = InkOnBackground,
    surfaceVariant = InkSurfaceVariant,
    onSurfaceVariant = InkOnSurfaceMuted,
    outline = InkOutline,
    outlineVariant = InkOutline.copy(alpha = 0.5f),
    error = InkDanger,
    onError = InkOnAccent,
    errorContainer = InkSurfaceVariant,
    onErrorContainer = InkDanger
)

private val PaperExtras = LedgerExtras(
    credit = PaperCredit,
    debit = PaperDebit,
    warning = PaperWarning,
    danger = PaperDanger,
    mutedText = PaperOnSurfaceMuted,
    hairline = PaperOutline
)

private val InkExtras = LedgerExtras(
    credit = InkCredit,
    debit = InkOnBackground,
    warning = InkWarning,
    danger = InkDanger,
    mutedText = InkOnSurfaceMuted,
    hairline = InkOutline
)

/**
 * Pocket Ledger Theme with Paper (Light) and Ink (Dark) modes.
 */
@Composable
fun LedgerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) InkColors else PaperColors
    val extras = if (darkTheme) InkExtras else PaperExtras

    CompositionLocalProvider(LocalLedgerExtras provides extras) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LedgerTypography,
            shapes = LedgerShapes,
            content = content
        )
    }
}

/**
 * Backward compatibility alias for ExpenseTrackerTheme
 */
@Composable
fun ExpenseTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    LedgerTheme(darkTheme = darkTheme, content = content)
}
