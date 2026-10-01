package com.example.easyespanol.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easyespanol.R

/*
 * TYPE
 * Headings: Bricolage Grotesque, a lively grotesque with a bit of character.
 * Everything else: Instrument Sans, clean and very readable at small sizes.
 * Both are open-source (SIL Open Font License), bundled in res/font, with their
 * licences in assets/licences.
 */
val DisplayFont = FontFamily(
    Font(R.font.bricolage_grotesque_regular, FontWeight.Normal),
    Font(R.font.bricolage_grotesque_bold, FontWeight.Bold),
)

val BodyFont = FontFamily(
    Font(R.font.instrument_sans_regular, FontWeight.Normal),
    Font(R.font.instrument_sans_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.instrument_sans_bold, FontWeight.Bold),
    Font(R.font.instrument_sans_bold_italic, FontWeight.Bold, FontStyle.Italic),
)

private fun display(size: Int, line: Int, tracking: Double = 0.0) = TextStyle(
    fontFamily = DisplayFont, fontWeight = FontWeight.Bold,
    fontSize = size.sp, lineHeight = line.sp, letterSpacing = tracking.sp,
)

private fun body(size: Int, line: Int, weight: FontWeight = FontWeight.Normal, tracking: Double = 0.0) = TextStyle(
    fontFamily = BodyFont, fontWeight = weight,
    fontSize = size.sp, lineHeight = line.sp, letterSpacing = tracking.sp,
)

val Typography = Typography(
    displayLarge = display(54, 58, -1.5),
    displayMedium = display(44, 48, -1.0),
    displaySmall = display(36, 42, -0.5),
    headlineLarge = display(30, 36, -0.5),
    headlineMedium = display(26, 32, -0.3),
    headlineSmall = display(23, 29, -0.2),
    titleLarge = display(20, 26),
    titleMedium = body(16, 22, FontWeight.Bold),
    titleSmall = body(14, 20, FontWeight.Bold),
    bodyLarge = body(16, 24),
    bodyMedium = body(14, 21),
    bodySmall = body(12, 17),
    labelLarge = body(14, 20, FontWeight.Bold, 0.1),
    labelMedium = body(12, 16, FontWeight.Bold, 0.2),
    labelSmall = body(11, 14, FontWeight.Bold, 0.3),
)

/** The phrase on a study card: large, airy, with the coloured chunks in bold. */
val PhraseTextStyle = TextStyle(
    fontFamily = BodyFont, fontWeight = FontWeight.Normal,
    fontSize = 25.sp, lineHeight = 34.sp,
)

/** Corner rounding: small things are tighter, big surfaces softer. */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)
