package uk.ac.tees.mad.payclock.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Define Theme choices
enum class ThemeChoice(val label: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System"),
    COLORFUL("Colorful")
}

// Colorful Light Theme Colors
private val ColorfulLightColorScheme = lightColorScheme(
    primary = Color(0xFF006A6A),      // Deep teal
    onPrimary = Color.White,
    primaryContainer = Color(0xFF9EEBE7),
    onPrimaryContainer = Color(0xFF00201F),

    secondary = Color(0xFFB02F00),    // Deep orange
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDBCF),
    onSecondaryContainer = Color(0xFF3B0900),

    tertiary = Color(0xFF4758A9),     // Vibrant blue
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDDE1FF),
    onTertiaryContainer = Color(0xFF001258),

    background = Color(0xFFFAFDFC),
    onBackground = Color(0xFF191C1C),

    surface = Color(0xFFFAFDFC),
    onSurface = Color(0xFF191C1C),

    surfaceVariant = Color(0xFFDAE5E3),
    onSurfaceVariant = Color(0xFF3F4948),

    outline = Color(0xFF6F7978),

    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

// Colorful Dark Theme Colors
private val ColorfulDarkColorScheme = darkColorScheme(
    primary = Color(0xFF4DD9D3),      // Bright teal
    onPrimary = Color(0xFF003735),
    primaryContainer = Color(0xFF00504D),
    onPrimaryContainer = Color(0xFF9EEBE7),

    secondary = Color(0xFFFFB59B),    // Light orange
    onSecondary = Color(0xFF5E1700),
    secondaryContainer = Color(0xFF872300),
    onSecondaryContainer = Color(0xFFFFDBCF),

    tertiary = Color(0xFFB8C4FF),     // Light blue
    onTertiary = Color(0xFF142778),
    tertiaryContainer = Color(0xFF2D4090),
    onTertiaryContainer = Color(0xFFDDE1FF),

    background = Color(0xFF191C1C),
    onBackground = Color(0xFFE0E3E1),

    surface = Color(0xFF191C1C),
    onSurface = Color(0xFFE0E3E1),

    surfaceVariant = Color(0xFF3F4948),
    onSurfaceVariant = Color(0xFFBEC9C7),

    outline = Color(0xFF899392),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

// Modern Light Theme Colors
private val ModernLightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),

    secondary = Color(0xFF625B71),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),

    tertiary = Color(0xFF7D5260),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),

    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),

    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),

    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),

    outline = Color(0xFF79747E),

    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
)

// Modern Dark Theme Colors
private val ModernDarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF371E73),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),

    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),

    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),

    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),

    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),

    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),

    outline = Color(0xFF938F99),

    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
)

// Classic Light Theme Colors (PayClock Brand)
private val ClassicLightColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32),      // Green
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA5D6A7),
    onPrimaryContainer = Color(0xFF0A200A),

    secondary = Color(0xFF1565C0),    // Blue
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFBBDEFB),
    onSecondaryContainer = Color(0xFF001E30),

    tertiary = Color(0xFF6A1B9A),     // Purple
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE1BEE7),
    onTertiaryContainer = Color(0xFF240046),

    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF1A1A1A),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1A1A),

    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF424242),

    outline = Color(0xFF757575),

    error = Color(0xFFD32F2F),
    onError = Color.White,
    errorContainer = Color(0xFFFFCDD2),
    onErrorContainer = Color(0xFF410002),
)

// Classic Dark Theme Colors (PayClock Brand)
private val ClassicDarkColorScheme = darkColorScheme(
    primary = Color(0xFF4CAF50),      // Light Green
    onPrimary = Color(0xFF003900),
    primaryContainer = Color(0xFF005B00),
    onPrimaryContainer = Color(0xFFA5D6A7),

    secondary = Color(0xFF64B5F6),    // Light Blue
    onSecondary = Color(0xFF003258),
    secondaryContainer = Color(0xFF004A7C),
    onSecondaryContainer = Color(0xFFBBDEFB),

    tertiary = Color(0xFFBA68C8),     // Light Purple
    onTertiary = Color(0xFF2A0057),
    tertiaryContainer = Color(0xFF420075),
    onTertiaryContainer = Color(0xFFE1BEE7),

    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),

    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),

    surfaceVariant = Color(0xFF424242),
    onSurfaceVariant = Color(0xFFC2C2C2),

    outline = Color(0xFF8C8C8C),

    error = Color(0xFFF44336),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFCDD2),
)

// Theme Manager
object ThemeManager {
    @Composable
    fun getColorScheme(
        themeChoice: ThemeChoice = ThemeChoice.SYSTEM,
        useDynamicColor: Boolean = true,
        isDarkTheme: Boolean = isSystemInDarkTheme()
    ): androidx.compose.material3.ColorScheme {
        val context = LocalContext.current

        // Determine if we should use dark theme
        val shouldUseDarkTheme = when (themeChoice) {
            ThemeChoice.LIGHT -> false
            ThemeChoice.DARK -> true
            ThemeChoice.SYSTEM -> isDarkTheme
            ThemeChoice.COLORFUL -> isDarkTheme // Use dark mode for colorful theme based on system setting
        }

        // Try dynamic colors first if enabled and supported
        if (useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return if (shouldUseDarkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }

        // Fall back to static color schemes based on theme choice
        return when (themeChoice) {
            ThemeChoice.COLORFUL -> {
                if (shouldUseDarkTheme) ColorfulDarkColorScheme else ColorfulLightColorScheme
            }

            ThemeChoice.LIGHT -> {
                ModernLightColorScheme
            }

            ThemeChoice.DARK -> {
                ModernDarkColorScheme
            }

            ThemeChoice.SYSTEM -> {
                if (shouldUseDarkTheme) ClassicDarkColorScheme else ClassicLightColorScheme
            }
        }
    }
}

@Composable
fun PayClockTheme(
    themeChoice: ThemeChoice = ThemeChoice.SYSTEM,
    useDynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val colorScheme = ThemeManager.getColorScheme(themeChoice, useDynamicColor, isDarkTheme)
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                !isDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}