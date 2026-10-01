package com.example.easyespanol.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/*
 * COLOUR SCHEMES
 *
 * The learner picks one of these in Settings. To change a palette, edit the hex
 * codes below (0xFF followed by the usual six-digit colour). To add a palette,
 * add an entry to AppPalette and a matching pair of schemes in paletteScheme().
 *
 * Why EasyJapanesey came out brown: its theme had dynamicColor = true, which on
 * Android 12+ ignores the app's colours and builds a scheme from the phone's
 * wallpaper. The emulator's default wallpaper is blue; your phone's gave brown.
 * Here that behaviour is only used if the learner chooses "Match my wallpaper".
 */
enum class AppPalette(val label: String, val description: String, val swatch: List<Color>) {
    SPAIN("Rojo y oro", "Red and gold, like the flag", listOf(FlagRed, FlagGold)),
    SEA("Mediterráneo", "Calm sea blues", listOf(Color(0xFF00639B), Color(0xFF96CCFF))),
    OLIVE("Olivo", "Olive green and terracotta", listOf(Color(0xFF4C6700), Color(0xFF9C4323))),
    WALLPAPER("Match my wallpaper", "Android 12 or newer: colours taken from your wallpaper", emptyList()),
}

enum class ThemeMode(val label: String) {
    SYSTEM("Follow phone"),
    LIGHT("Light"),
    DARK("Dark"),
}

/** The chunk colours for the current light/dark mode, available to any screen. */
val LocalChunkColours = staticCompositionLocalOf { ChunkColoursLight }

@Composable
fun EasyEspanolTheme(
    palette: AppPalette,
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        palette == AppPalette.WALLPAPER && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        else -> paletteScheme(palette, darkTheme)
    }
    CompositionLocalProvider(
        LocalChunkColours provides if (darkTheme) ChunkColoursDark else ChunkColoursLight
    ) {
        MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
    }
}

private fun paletteScheme(palette: AppPalette, dark: Boolean): ColorScheme = when (palette) {
    AppPalette.SEA -> if (!dark) scheme(
        dark = false,
        primary = 0xFF00639B, onPrimary = 0xFFFFFFFF, primaryContainer = 0xFFCEE5FF, onPrimaryContainer = 0xFF001D33,
        secondary = 0xFF51606F, onSecondary = 0xFFFFFFFF, secondaryContainer = 0xFFD5E4F7, onSecondaryContainer = 0xFF0E1D2A,
        tertiary = 0xFF006A60, onTertiary = 0xFFFFFFFF, tertiaryContainer = 0xFF74F8E5, onTertiaryContainer = 0xFF00201C,
        background = 0xFFF7F9FF, onBackground = 0xFF181C20,
        surfaceVariant = 0xFFDEE3EB, onSurfaceVariant = 0xFF42474E, outline = 0xFF72787E,
        containers = listOf(0xFFFFFFFF, 0xFFF1F4F9, 0xFFEBEEF3, 0xFFE6E8EE, 0xFFE0E2E8),
    ) else scheme(
        dark = true,
        primary = 0xFF96CCFF, onPrimary = 0xFF003353, primaryContainer = 0xFF004A76, onPrimaryContainer = 0xFFCEE5FF,
        secondary = 0xFFB9C8DA, onSecondary = 0xFF233240, secondaryContainer = 0xFF3A4857, onSecondaryContainer = 0xFFD5E4F7,
        tertiary = 0xFF53DBC9, onTertiary = 0xFF003731, tertiaryContainer = 0xFF005048, onTertiaryContainer = 0xFF74F8E5,
        background = 0xFF101418, onBackground = 0xFFE0E2E8,
        surfaceVariant = 0xFF42474E, onSurfaceVariant = 0xFFC2C7CF, outline = 0xFF8C9199,
        containers = listOf(0xFF0B0F13, 0xFF181C20, 0xFF1C2024, 0xFF272A2F, 0xFF31353A),
    )

    AppPalette.OLIVE -> if (!dark) scheme(
        dark = false,
        primary = 0xFF4C6700, onPrimary = 0xFFFFFFFF, primaryContainer = 0xFFCCF080, onPrimaryContainer = 0xFF141F00,
        secondary = 0xFF5A6147, onSecondary = 0xFFFFFFFF, secondaryContainer = 0xFFDEE6C4, onSecondaryContainer = 0xFF171E09,
        tertiary = 0xFF9C4323, onTertiary = 0xFFFFFFFF, tertiaryContainer = 0xFFFFDBCF, onTertiaryContainer = 0xFF390C00,
        background = 0xFFFAFAEE, onBackground = 0xFF1B1C15,
        surfaceVariant = 0xFFE2E4D3, onSurfaceVariant = 0xFF45483C, outline = 0xFF76786B,
        containers = listOf(0xFFFFFFFF, 0xFFF4F4E8, 0xFFEEEFE3, 0xFFE9E9DD, 0xFFE3E3D8),
    ) else scheme(
        dark = true,
        primary = 0xFFB1D367, onPrimary = 0xFF253500, primaryContainer = 0xFF384E00, onPrimaryContainer = 0xFFCCF080,
        secondary = 0xFFC2CAA9, onSecondary = 0xFF2C331D, secondaryContainer = 0xFF424A31, onSecondaryContainer = 0xFFDEE6C4,
        tertiary = 0xFFFFB59C, onTertiary = 0xFF5C1900, tertiaryContainer = 0xFF7D2C0E, onTertiaryContainer = 0xFFFFDBCF,
        background = 0xFF12140D, onBackground = 0xFFE3E3D8,
        surfaceVariant = 0xFF45483C, onSurfaceVariant = 0xFFC6C8B8, outline = 0xFF8F9284,
        containers = listOf(0xFF0D0F08, 0xFF1B1C15, 0xFF1F2019, 0xFF292B23, 0xFF34352D),
    )

    // SPAIN, and the fallback for WALLPAPER on phones older than Android 12.
    else -> if (!dark) scheme(
        dark = false,
        primary = 0xFFAA151B, onPrimary = 0xFFFFFFFF, primaryContainer = 0xFFFFDAD6, onPrimaryContainer = 0xFF410003,
        secondary = 0xFF765A00, onSecondary = 0xFFFFFFFF, secondaryContainer = 0xFFFFDF93, onSecondaryContainer = 0xFF241A00,
        tertiary = 0xFF8B5000, onTertiary = 0xFFFFFFFF, tertiaryContainer = 0xFFFFDCBE, onTertiaryContainer = 0xFF2C1600,
        background = 0xFFFFF8F6, onBackground = 0xFF231918,
        surfaceVariant = 0xFFF5DDDA, onSurfaceVariant = 0xFF534341, outline = 0xFF857371,
        containers = listOf(0xFFFFFFFF, 0xFFFFF0EE, 0xFFFCEAE7, 0xFFF6E4E2, 0xFFF1DEDC),
    ) else scheme(
        dark = true,
        primary = 0xFFFFB4AB, onPrimary = 0xFF690005, primaryContainer = 0xFF93000A, onPrimaryContainer = 0xFFFFDAD6,
        secondary = 0xFFF1BF00, onSecondary = 0xFF3E2E00, secondaryContainer = 0xFF5A4300, onSecondaryContainer = 0xFFFFDF93,
        tertiary = 0xFFFFB870, onTertiary = 0xFF4A2800, tertiaryContainer = 0xFF6A3B00, onTertiaryContainer = 0xFFFFDCBE,
        background = 0xFF1A1110, onBackground = 0xFFF1DEDC,
        surfaceVariant = 0xFF534341, onSurfaceVariant = 0xFFD8C2BF, outline = 0xFFA08C8A,
        containers = listOf(0xFF140C0B, 0xFF231918, 0xFF271D1C, 0xFF322826, 0xFF3D3231),
    )
}

/**
 * Builds a full Material 3 scheme from the main colours. `containers` are the five
 * surface shades used by cards and sheets, from lowest to highest. Setting them
 * stops Material's default purple-grey from showing through on cards.
 */
private fun scheme(
    dark: Boolean,
    primary: Long, onPrimary: Long, primaryContainer: Long, onPrimaryContainer: Long,
    secondary: Long, onSecondary: Long, secondaryContainer: Long, onSecondaryContainer: Long,
    tertiary: Long, onTertiary: Long, tertiaryContainer: Long, onTertiaryContainer: Long,
    background: Long, onBackground: Long,
    surfaceVariant: Long, onSurfaceVariant: Long, outline: Long,
    containers: List<Long>,
): ColorScheme {
    val c = containers.map { Color(it) }
    return if (dark) darkColorScheme(
        primary = Color(primary), onPrimary = Color(onPrimary),
        primaryContainer = Color(primaryContainer), onPrimaryContainer = Color(onPrimaryContainer),
        secondary = Color(secondary), onSecondary = Color(onSecondary),
        secondaryContainer = Color(secondaryContainer), onSecondaryContainer = Color(onSecondaryContainer),
        tertiary = Color(tertiary), onTertiary = Color(onTertiary),
        tertiaryContainer = Color(tertiaryContainer), onTertiaryContainer = Color(onTertiaryContainer),
        background = Color(background), onBackground = Color(onBackground),
        surface = Color(background), onSurface = Color(onBackground),
        surfaceVariant = Color(surfaceVariant), onSurfaceVariant = Color(onSurfaceVariant),
        outline = Color(outline), outlineVariant = Color(surfaceVariant),
        surfaceContainerLowest = c[0], surfaceContainerLow = c[1], surfaceContainer = c[2],
        surfaceContainerHigh = c[3], surfaceContainerHighest = c[4],
    ) else lightColorScheme(
        primary = Color(primary), onPrimary = Color(onPrimary),
        primaryContainer = Color(primaryContainer), onPrimaryContainer = Color(onPrimaryContainer),
        secondary = Color(secondary), onSecondary = Color(onSecondary),
        secondaryContainer = Color(secondaryContainer), onSecondaryContainer = Color(onSecondaryContainer),
        tertiary = Color(tertiary), onTertiary = Color(onTertiary),
        tertiaryContainer = Color(tertiaryContainer), onTertiaryContainer = Color(onTertiaryContainer),
        background = Color(background), onBackground = Color(onBackground),
        surface = Color(background), onSurface = Color(onBackground),
        surfaceVariant = Color(surfaceVariant), onSurfaceVariant = Color(onSurfaceVariant),
        outline = Color(outline), outlineVariant = Color(surfaceVariant),
        surfaceContainerLowest = c[0], surfaceContainerLow = c[1], surfaceContainer = c[2],
        surfaceContainerHigh = c[3], surfaceContainerHighest = c[4],
    )
}
