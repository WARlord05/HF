package com.expensetracker.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Pocket Ledger Typography Specification.
 * Pairs a characterful serif (Newsreader / Serif) for editorial headline moments & totals
 * with a clean sans-serif (Inter / SansSerif) for UI, and tabular numbers for amounts.
 */

val SerifFamily = FontFamily.Serif
val SansFamily = FontFamily.SansSerif

val DisplayTotalStyle = TextStyle(
    fontFamily = SerifFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 40.sp,
    lineHeight = 44.sp,
    fontFeatureSettings = "tnum"
)

val ScreenTitleStyle = TextStyle(
    fontFamily = SerifFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 28.sp,
    lineHeight = 34.sp
)

val MonthHeaderStyle = TextStyle(
    fontFamily = SerifFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 22.sp,
    lineHeight = 28.sp
)

val SectionLabelStyle = TextStyle(
    fontFamily = SansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.08.sp * 12 // +8% tracking
)

val RowTitleStyle = TextStyle(
    fontFamily = SansFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 22.sp
)

val RowAmountStyle = TextStyle(
    fontFamily = SansFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 22.sp,
    fontFeatureSettings = "tnum"
)

val BodyStyle = TextStyle(
    fontFamily = SansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 22.sp
)

val CaptionStyle = TextStyle(
    fontFamily = SansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp
)

val LedgerTypography = Typography(
    displayLarge = DisplayTotalStyle,
    headlineMedium = ScreenTitleStyle,
    headlineSmall = MonthHeaderStyle,
    titleMedium = RowTitleStyle,
    bodyLarge = BodyStyle,
    bodyMedium = TextStyle(
        fontFamily = SansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = CaptionStyle,
    labelLarge = SectionLabelStyle,
    labelMedium = TextStyle(
        fontFamily = SansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelSmall = CaptionStyle
)
