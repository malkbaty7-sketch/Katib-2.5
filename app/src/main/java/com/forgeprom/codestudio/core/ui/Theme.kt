package com.forgeprom.codestudio.core.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

val PurpleBrandPrimary = Color(0xFF6B46C1) // Deep Purple "F"
val PurpleBrandAccent = Color(0xFF9F7AEA)
val ObsidianDarkBg = Color(0xFF0F0B1E)
val ObsidianDarkSurface = Color(0xFF18122B)
val TextPrimaryDark = Color(0xFFF7FAFC)
val TextSecondaryDark = Color(0xFFA0AEC0)

private val DarkColorScheme = darkColorScheme(
    primary = PurpleBrandPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3B1E78),
    secondary = PurpleBrandAccent,
    background = ObsidianDarkBg,
    surface = ObsidianDarkSurface,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = PurpleBrandPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    secondary = PurpleBrandAccent,
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A)
)

/**
 * سمة التطبيق القياسية (ForgepromTheme)
 * تفرض اتجاه اليمين لليسار (RTL) للغة العربية كأساس متين، وتدعم النصوص البرمجية المختلطة (LTR).
 */
@Composable
fun ForgepromTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
