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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/*
 * COLOUR SCHEMES
 *
 * Each palette has two parts:
 *  - a Material colour scheme (light and dark) used by buttons, text, cards...
 *  - a Brand: the gradient colours for the home tile and main buttons, the
 *    background glows, and the accent colours given to topics in turn.
 *
 * To change a palette, edit its hex codes (0xFF + the usual six-digit colour).
 * To add one, add an entry to AppPalette, a brand in brandFor() and a scheme in
 * paletteScheme(). It then appears in Settings automatically.
 *
 * "Match my wallpaper" uses Android 12's dynamic colour, which builds the scheme
 * from the phone's wallpaper. That is what turned EasyJapanesey brown.
 */
enum class AppPalette(val label: String, val description: String) {
    ATARDECER("Atardecer", "Sunset over the sea: cobalt, bougainvillea and coral"),
    ROJO("Rojo y oro", "Crimson and gold, like the flag"),
    AZULEJO("Azulejo", "Glazed-tile blue and sea glass"),
    JACARANDA("Jacaranda", "Violet blossom and pink"),
    OLIVO("Olivo", "Olive groves in late sun"),
    WALLPAPER("Match my wallpaper", "Colours taken from your wallpaper (Android 12 or newer)"),
}

enum class ThemeMode(val label: String) {
    SYSTEM("Automatic"),
    LIGHT("Light"),
    DARK("Dark"),
}

@Immutable
data class Brand(
    /** The signature gradient: home tile, progress bars. */
    val hero: List<Color>,
    /** Main-button gradient. Darker than hero so white text always reads. */
    val button: List<Color>,
    /** Topic colours, used in rotation. */
    val accents: List<Color>,
    val known: Color,
    val learning: Color,
    /** Soft glows in the corners of the background. */
    val glowA: Color,
    val glowB: Color,
    val dark: Boolean,
)

/** The current brand colours, available to any screen. */
val LocalBrand = staticCompositionLocalOf { brandFor(AppPalette.ATARDECER, false, lightColorScheme()) }

/** The chunk colours for the current light/dark mode. */
val LocalChunkColours = staticCompositionLocalOf { ChunkColoursLight }

@Composable
fun EasyEspanolTheme(
    palette: AppPalette,
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val useWallpaper = palette == AppPalette.WALLPAPER && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = when {
        useWallpaper && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        else -> paletteScheme(palette, darkTheme)
    }
    val brand = brandFor(if (useWallpaper) AppPalette.WALLPAPER else fixed(palette), darkTheme, colorScheme)
    CompositionLocalProvider(
        LocalBrand provides brand,
        LocalChunkColours provides if (darkTheme) ChunkColoursDark else ChunkColoursLight,
    ) {
        MaterialTheme(colorScheme = colorScheme, typography = Typography, shapes = AppShapes, content = content)
    }
}

/** On phones older than Android 12, "Match my wallpaper" falls back to the default. */
private fun fixed(palette: AppPalette) = if (palette == AppPalette.WALLPAPER) AppPalette.ATARDECER else palette

private fun c(hex: Long) = Color(hex)

fun brandFor(palette: AppPalette, dark: Boolean, scheme: ColorScheme): Brand {
    val known = if (dark) KnownDark else KnownLight
    val learning = if (dark) LearningDark else LearningLight
    return when (palette) {
        AppPalette.ROJO -> Brand(
            hero = listOf(c(0xFF6E0A1A), c(0xFFB3122E), c(0xFFE3342F), c(0xFFF07A1E)),
            button = listOf(c(0xFF8E0E24), c(0xFFC8102E), c(0xFFE0402A)),
            accents = if (dark) listOf(c(0xFFFF8A93), c(0xFFFFC14D), c(0xFF8EB8FF), c(0xFF4FD1A5), c(0xFFD59AF0), c(0xFFFF9C73))
            else listOf(c(0xFFC8102E), c(0xFFD98800), c(0xFF1F5FA8), c(0xFF13926B), c(0xFF8E3FA8), c(0xFFD9542B)),
            known = known, learning = learning, glowA = c(0xFFE3342F), glowB = c(0xFFF4A100), dark = dark,
        )
        AppPalette.AZULEJO -> Brand(
            hero = listOf(c(0xFF0B3D91), c(0xFF1565C0), c(0xFF1598B4), c(0xFF22B5A4)),
            button = listOf(c(0xFF0B3D91), c(0xFF1565C0), c(0xFF0E7F96)),
            accents = if (dark) listOf(c(0xFF8EC5FF), c(0xFF5ED6DE), c(0xFFFF8FAE), c(0xFFFFC14D), c(0xFFA3AEFF), c(0xFF4FD1A5))
            else listOf(c(0xFF1565C0), c(0xFF0E8C98), c(0xFFD9466E), c(0xFFD98800), c(0xFF3949AB), c(0xFF13926B)),
            known = known, learning = learning, glowA = c(0xFF1598B4), glowB = c(0xFF3949AB), dark = dark,
        )
        AppPalette.JACARANDA -> Brand(
            hero = listOf(c(0xFF3C1C9C), c(0xFF7038D1), c(0xFFB8399E), c(0xFFE2557F)),
            button = listOf(c(0xFF4A23B0), c(0xFF7038D1), c(0xFFA8328F)),
            accents = if (dark) listOf(c(0xFFC6B0FF), c(0xFFFF82BE), c(0xFFFFC14D), c(0xFF4FD1A5), c(0xFF5CCBF0), c(0xFFDDA6FF))
            else listOf(c(0xFF6D3FD1), c(0xFFC2347E), c(0xFFD98800), c(0xFF13926B), c(0xFF0B87AD), c(0xFF9C3FD1)),
            known = known, learning = learning, glowA = c(0xFFB8399E), glowB = c(0xFF7038D1), dark = dark,
        )
        AppPalette.OLIVO -> Brand(
            hero = listOf(c(0xFF2F4600), c(0xFF4C6700), c(0xFF7E8A16), c(0xFFB98E14)),
            button = listOf(c(0xFF2F4600), c(0xFF4C6700), c(0xFF6E7D10)),
            accents = if (dark) listOf(c(0xFFB1D367), c(0xFFFFD267), c(0xFFFFB59C), c(0xFF5ED6BE), c(0xFFCBE38A), c(0xFF9EC2FF))
            else listOf(c(0xFF4C6700), c(0xFFA87F00), c(0xFF9C4323), c(0xFF13866E), c(0xFF6E8B1E), c(0xFF3F6FA8)),
            known = known, learning = learning, glowA = c(0xFFB98E14), glowB = c(0xFF6E8B1E), dark = dark,
        )
        AppPalette.WALLPAPER -> {
            val start = if (dark) scheme.primaryContainer else scheme.primary
            val end = if (dark) scheme.tertiaryContainer else scheme.tertiary
            Brand(
                hero = listOf(start, end), button = listOf(start, end),
                accents = listOf(scheme.primary, scheme.tertiary, scheme.secondary, known, learning, scheme.error),
                known = known, learning = learning, glowA = scheme.tertiary, glowB = scheme.primary, dark = dark,
            )
        }
        // ATARDECER, the default
        else -> Brand(
            hero = listOf(c(0xFF2D4BD8), c(0xFF6B3FD8), c(0xFFD63384), c(0xFFF26B3A)),
            button = listOf(c(0xFF2D4BD8), c(0xFF6B3FD8), c(0xFFC22C78)),
            accents = if (dark) listOf(c(0xFF9AAAFF), c(0xFFFF7DB6), c(0xFFFFC14D), c(0xFF4FD1A5), c(0xFFBFA2FF), c(0xFF5CCBF0))
            else listOf(c(0xFF2D4BD8), c(0xFFD63384), c(0xFFD98800), c(0xFF13926B), c(0xFF7B3FD6), c(0xFF0B87AD)),
            known = known, learning = learning, glowA = c(0xFFD63384), glowB = c(0xFFF49A00), dark = dark,
        )
    }
}

private fun paletteScheme(palette: AppPalette, dark: Boolean): ColorScheme = when (fixed(palette)) {
    AppPalette.ROJO -> if (!dark) scheme(
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

    AppPalette.AZULEJO -> if (!dark) scheme(
        dark = false,
        primary = 0xFF00629E, onPrimary = 0xFFFFFFFF, primaryContainer = 0xFFCEE5FF, onPrimaryContainer = 0xFF001D33,
        secondary = 0xFF51606F, onSecondary = 0xFFFFFFFF, secondaryContainer = 0xFFD5E4F7, onSecondaryContainer = 0xFF0E1D2A,
        tertiary = 0xFF006A60, onTertiary = 0xFFFFFFFF, tertiaryContainer = 0xFF74F8E5, onTertiaryContainer = 0xFF00201C,
        background = 0xFFF5F9FC, onBackground = 0xFF0F1D2B,
        surfaceVariant = 0xFFDEE3EB, onSurfaceVariant = 0xFF42474E, outline = 0xFF72787E,
        containers = listOf(0xFFFFFFFF, 0xFFF0F5F9, 0xFFEAF0F5, 0xFFE4EAF0, 0xFFDEE4EA),
    ) else scheme(
        dark = true,
        primary = 0xFF96CCFF, onPrimary = 0xFF003353, primaryContainer = 0xFF004A76, onPrimaryContainer = 0xFFCEE5FF,
        secondary = 0xFFB9C8DA, onSecondary = 0xFF233240, secondaryContainer = 0xFF3A4857, onSecondaryContainer = 0xFFD5E4F7,
        tertiary = 0xFF53DBC9, onTertiary = 0xFF003731, tertiaryContainer = 0xFF005048, onTertiaryContainer = 0xFF74F8E5,
        background = 0xFF0B1826, onBackground = 0xFFE0E6EE,
        surfaceVariant = 0xFF42474E, onSurfaceVariant = 0xFFC2C7CF, outline = 0xFF8C9199,
        containers = listOf(0xFF07121D, 0xFF12202E, 0xFF162534, 0xFF1F2F3F, 0xFF2A3A4B),
    )

    AppPalette.JACARANDA -> if (!dark) scheme(
        dark = false,
        primary = 0xFF6D3FD1, onPrimary = 0xFFFFFFFF, primaryContainer = 0xFFEADDFF, onPrimaryContainer = 0xFF24005A,
        secondary = 0xFF625B71, onSecondary = 0xFFFFFFFF, secondaryContainer = 0xFFE8DEF8, onSecondaryContainer = 0xFF1E192B,
        tertiary = 0xFFA8326E, onTertiary = 0xFFFFFFFF, tertiaryContainer = 0xFFFFD8E7, onTertiaryContainer = 0xFF3D0024,
        background = 0xFFFAF8FF, onBackground = 0xFF1C1A2B,
        surfaceVariant = 0xFFE7E0EF, onSurfaceVariant = 0xFF49454F, outline = 0xFF7A757F,
        containers = listOf(0xFFFFFFFF, 0xFFF5F2FC, 0xFFEFEBF7, 0xFFE9E5F2, 0xFFE3DFEC),
    ) else scheme(
        dark = true,
        primary = 0xFFD2BCFF, onPrimary = 0xFF3C0091, primaryContainer = 0xFF5427B8, onPrimaryContainer = 0xFFEADDFF,
        secondary = 0xFFCCC2DC, onSecondary = 0xFF332D41, secondaryContainer = 0xFF4A4458, onSecondaryContainer = 0xFFE8DEF8,
        tertiary = 0xFFFFAFD3, onTertiary = 0xFF5F113E, tertiaryContainer = 0xFF861A57, onTertiaryContainer = 0xFFFFD8E7,
        background = 0xFF15112A, onBackground = 0xFFE8E2F2,
        surfaceVariant = 0xFF49454F, onSurfaceVariant = 0xFFCAC4D0, outline = 0xFF948F99,
        containers = listOf(0xFF100C22, 0xFF1D1834, 0xFF221D3A, 0xFF2C2645, 0xFF373150),
    )

    AppPalette.OLIVO -> if (!dark) scheme(
        dark = false,
        primary = 0xFF4C6700, onPrimary = 0xFFFFFFFF, primaryContainer = 0xFFCCF080, onPrimaryContainer = 0xFF141F00,
        secondary = 0xFF5A6147, onSecondary = 0xFFFFFFFF, secondaryContainer = 0xFFDEE6C4, onSecondaryContainer = 0xFF171E09,
        tertiary = 0xFF9C4323, onTertiary = 0xFFFFFFFF, tertiaryContainer = 0xFFFFDBCF, onTertiaryContainer = 0xFF390C00,
        background = 0xFFF9FAF0, onBackground = 0xFF1B1C15,
        surfaceVariant = 0xFFE2E4D3, onSurfaceVariant = 0xFF45483C, outline = 0xFF76786B,
        containers = listOf(0xFFFFFFFF, 0xFFF4F5E9, 0xFFEEEFE3, 0xFFE9E9DD, 0xFFE3E3D8),
    ) else scheme(
        dark = true,
        primary = 0xFFB1D367, onPrimary = 0xFF253500, primaryContainer = 0xFF384E00, onPrimaryContainer = 0xFFCCF080,
        secondary = 0xFFC2CAA9, onSecondary = 0xFF2C331D, secondaryContainer = 0xFF424A31, onSecondaryContainer = 0xFFDEE6C4,
        tertiary = 0xFFFFB59C, onTertiary = 0xFF5C1900, tertiaryContainer = 0xFF7D2C0E, onTertiaryContainer = 0xFFFFDBCF,
        background = 0xFF12140D, onBackground = 0xFFE3E3D8,
        surfaceVariant = 0xFF45483C, onSurfaceVariant = 0xFFC6C8B8, outline = 0xFF8F9284,
        containers = listOf(0xFF0D0F08, 0xFF1B1C15, 0xFF1F2019, 0xFF292B23, 0xFF34352D),
    )

    // ATARDECER, the default: cobalt glaze, saffron and bougainvillea on whitewash.
    else -> if (!dark) scheme(
        dark = false,
        primary = 0xFF2D4BD8, onPrimary = 0xFFFFFFFF, primaryContainer = 0xFFDDE1FF, onPrimaryContainer = 0xFF001257,
        secondary = 0xFF7A5900, onSecondary = 0xFFFFFFFF, secondaryContainer = 0xFFFFDEA6, onSecondaryContainer = 0xFF261900,
        tertiary = 0xFFB5165E, onTertiary = 0xFFFFFFFF, tertiaryContainer = 0xFFFFD9E2, onTertiaryContainer = 0xFF3E0020,
        background = 0xFFF6F7FB, onBackground = 0xFF141B3A,
        surfaceVariant = 0xFFE2E4F0, onSurfaceVariant = 0xFF464A5E, outline = 0xFF767A8F,
        containers = listOf(0xFFFFFFFF, 0xFFF1F2F9, 0xFFECEDF6, 0xFFE6E8F2, 0xFFE0E2EE),
    ) else scheme(
        dark = true,
        primary = 0xFFB9C3FF, onPrimary = 0xFF00218F, primaryContainer = 0xFF1F37B8, onPrimaryContainer = 0xFFDDE1FF,
        secondary = 0xFFFFBA2E, onSecondary = 0xFF402D00, secondaryContainer = 0xFF5C4200, onSecondaryContainer = 0xFFFFDEA6,
        tertiary = 0xFFFFB1C8, onTertiary = 0xFF650033, tertiaryContainer = 0xFF8E0E4B, onTertiaryContainer = 0xFFFFD9E2,
        background = 0xFF0F1430, onBackground = 0xFFE3E5F5,
        surfaceVariant = 0xFF3F4359, onSurfaceVariant = 0xFFC5C7DA, outline = 0xFF8F92A6,
        containers = listOf(0xFF0A0E26, 0xFF161B3A, 0xFF1A2040, 0xFF242A4C, 0xFF2F3557),
    )
}

/**
 * Builds a full Material 3 scheme from the main colours. `containers` are the five
 * surface shades used by cards and sheets, from lowest to highest. Setting them
 * stops Material's default purple-grey from showing through.
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
    val s = containers.map { Color(it) }
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
        surfaceContainerLowest = s[0], surfaceContainerLow = s[1], surfaceContainer = s[2],
        surfaceContainerHigh = s[3], surfaceContainerHighest = s[4],
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
        surfaceContainerLowest = s[0], surfaceContainerLow = s[1], surfaceContainer = s[2],
        surfaceContainerHigh = s[3], surfaceContainerHighest = s[4],
    )
}
