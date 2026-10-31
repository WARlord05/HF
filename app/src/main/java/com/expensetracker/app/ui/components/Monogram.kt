package com.expensetracker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensetracker.app.ui.theme.CategoryColors

/**
 * Monogram circle: Displays a 1-2 letter initial inside a muted, category-tinted circle.
 * Avoids noisy emojis and generic stock icons in favor of a calm, editorial ledger style.
 */
@Composable
fun Monogram(
    category: String,
    modifier: Modifier = Modifier,
    name: String = "",
    size: Dp = 40.dp
) {
    val categoryColor = CategoryColors.colorFor(category)
    val initials = extractInitials(name, category)

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(categoryColor.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = categoryColor,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = (size.value * 0.36f).sp,
            letterSpacing = 0.5.sp
        )
    }
}

private fun extractInitials(name: String, category: String): String {
    val trimmedName = name.trim()
    if (trimmedName.isNotEmpty()) {
        val parts = trimmedName.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        if (parts.size >= 2) {
            val first = parts[0].first().uppercaseChar()
            val second = parts[1].first().uppercaseChar()
            if (first.isLetterOrDigit() && second.isLetterOrDigit()) {
                return "$first$second"
            }
        }
        val cleanWord = trimmedName.filter { it.isLetterOrDigit() }
        if (cleanWord.length >= 2) {
            return cleanWord.take(2).uppercase()
        } else if (cleanWord.isNotEmpty()) {
            return cleanWord.take(1).uppercase()
        }
    }

    // Fallback to category initials
    val cleanCat = category.trim().filter { it.isLetter() }
    return if (cleanCat.length >= 2) cleanCat.take(2).uppercase() else cleanCat.take(1).uppercase().ifEmpty { "OT" }
}
