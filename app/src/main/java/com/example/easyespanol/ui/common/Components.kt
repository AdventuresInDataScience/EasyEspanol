@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.easyespanol.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easyespanol.data.Markup
import com.example.easyespanol.data.SpeakerGender
import com.example.easyespanol.ui.theme.LocalBrand
import com.example.easyespanol.ui.theme.LocalChunkColours
import kotlin.math.roundToInt

val PillShape = RoundedCornerShape(50)

// ── Backgrounds and screen frame ──────────────────────────────────────────

/** Every screen sits on this: a soft vertical wash with two faint colour glows. */
@Composable
fun AppBackground(content: @Composable BoxScope.() -> Unit) {
    val brand = LocalBrand.current
    val scheme = MaterialTheme.colorScheme
    val glowAlpha = if (brand.dark) 0.22f else 0.15f
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(scheme.background, scheme.surfaceContainer)))
            .drawBehind {
                val r = size.maxDimension * 0.6f
                val a = Offset(size.width * 1.0f, 0f)
                val b = Offset(0f, size.height)
                drawCircle(
                    brush = Brush.radialGradient(listOf(brand.glowA.copy(alpha = glowAlpha), Color.Transparent), center = a, radius = r),
                    radius = r, center = a,
                )
                drawCircle(
                    brush = Brush.radialGradient(listOf(brand.glowB.copy(alpha = glowAlpha), Color.Transparent), center = b, radius = r),
                    radius = r, center = b,
                )
            },
        content = content,
    )
}

/** A screen: background, optional header with a back button, and content. */
@Composable
fun AppScaffold(
    title: String? = null,
    onBack: (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            topBar = { if (title != null) ScreenHeader(title, onBack) },
            content = content,
        )
    }
}

@Composable
fun ScreenHeader(title: String, onBack: (() -> Unit)?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 12.dp, end = 20.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            FilledTonalIconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.width(12.dp))
        }
        Text(title, style = MaterialTheme.typography.headlineSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// ── Buttons ───────────────────────────────────────────────────────────────

/** The main call to action: a pill filled with the palette's gradient. */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val brand = LocalBrand.current
    val colours = if (enabled) brand.button else listOf(Color(0xFF9EA2B3), Color(0xFFB8BBC8))
    Box(
        modifier = modifier
            .heightIn(min = 58.dp)
            .shadow(if (enabled) 14.dp else 0.dp, PillShape, ambientColor = colours.first(), spotColor = colours.last())
            .clip(PillShape)
            .background(Brush.horizontalGradient(colours))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** A softer pill: a light wash of `colour` with darker text in the same hue. */
@Composable
fun TintButton(
    text: String,
    onClick: () -> Unit,
    colour: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val dark = LocalBrand.current.dark
    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.45f)
            .heightIn(min = 56.dp)
            .clip(PillShape)
            .background(colour.copy(alpha = if (dark) 0.22f else 0.13f))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, color = readableOn(colour, dark), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** A solid pill in `colour`, with black or white text, whichever reads better. */
@Composable
fun SolidButton(text: String, onClick: () -> Unit, colour: Color, modifier: Modifier = Modifier) {
    val textColour = if (colour.luminance() > 0.45f) Color(0xFF07231A) else Color.White
    Box(
        modifier = modifier
            .heightIn(min = 56.dp)
            .shadow(10.dp, PillShape, ambientColor = colour, spotColor = colour)
            .clip(PillShape)
            .background(colour)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, color = textColour, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Text in the hue of `colour`, darkened or lightened enough to read on a pale wash of it. */
fun readableOn(colour: Color, dark: Boolean): Color =
    if (dark) lerp(colour, Color.White, 0.25f) else lerp(colour, Color.Black, 0.35f)

// ── Cards, tiles and labels ───────────────────────────────────────────────

/** The standard card: white (or deep navy) with a hairline edge and a whisper of shadow. */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    padding: Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val brand = LocalBrand.current
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(24.dp)
    val colour = if (brand.dark) scheme.surfaceContainerHigh else scheme.surfaceContainerLowest
    val border = BorderStroke(1.dp, scheme.outline.copy(alpha = if (brand.dark) 0.20f else 0.10f))
    val elevation = if (brand.dark) 0.dp else 1.dp
    val inner: @Composable () -> Unit = { Column(Modifier.padding(padding), content = content) }
    if (onClick != null) {
        Surface(
            onClick = onClick, enabled = enabled, modifier = modifier, shape = shape,
            color = colour, border = border, shadowElevation = elevation, content = inner,
        )
    } else {
        Surface(
            modifier = modifier, shape = shape, color = colour, border = border,
            shadowElevation = elevation, content = inner,
        )
    }
}

/** An emoji (or, with monogram = true, a letter) on a rounded square washed in `colour`. */
@Composable
fun EmojiTile(emoji: String, colour: Color, size: Dp = 52.dp, monogram: Boolean = false) {
    val dark = LocalBrand.current.dark
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.32f))
            .background(colour.copy(alpha = if (dark) 0.26f else 0.15f)),
        contentAlignment = Alignment.Center,
    ) {
        if (monogram) {
            Text(
                emoji,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = (size.value * 0.44f).sp),
                color = readableOn(colour, dark),
            )
        } else {
            Text(emoji, fontSize = (size.value * 0.46f).sp)
        }
    }
}

/** A small rounded label. */
@Composable
fun Tag(text: String, colour: Color) {
    val dark = LocalBrand.current.dark
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = readableOn(colour, dark),
        modifier = Modifier
            .clip(PillShape)
            .background(colour.copy(alpha = if (dark) 0.24f else 0.13f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

/** A row in a list of topics or scenes: emoji tile, title, subtitle and a progress ring. */
@Composable
fun ProgressRow(
    emoji: String,
    colour: Color,
    title: String,
    subtitle: String,
    known: Int,
    total: Int,
    enabled: Boolean = true,
    monogram: Boolean = false,
    onClick: () -> Unit,
) {
    AppCard(
        modifier = Modifier.fillMaxWidth().alpha(if (enabled) 1f else 0.5f),
        onClick = onClick,
        enabled = enabled,
        padding = 14.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EmojiTile(emoji, colour, monogram = monogram)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (total > 0) {
                Spacer(Modifier.width(12.dp))
                ProgressRing(
                    progress = known.toFloat() / total,
                    modifier = Modifier.size(46.dp),
                    stroke = 5.dp,
                    colours = listOf(colour),
                    track = colour.copy(alpha = 0.16f),
                    animate = false,
                ) {
                    Text(percentText(known, total), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

fun percentText(known: Int, total: Int): String {
    if (total == 0) return "0%"
    val percent = (known * 100f / total).roundToInt()
    return if (known > 0 && percent == 0) "<1%" else "$percent%"
}

// ── Progress ──────────────────────────────────────────────────────────────

/** A circular progress ring. With animate = true it sweeps round when first shown. */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    stroke: Dp = 8.dp,
    colours: List<Color>,
    track: Color,
    animate: Boolean = true,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val target = progress.coerceIn(0f, 1f)
    val animated = remember { Animatable(if (animate) 0f else target) }
    LaunchedEffect(target) {
        if (animate) animated.animateTo(target, tween(durationMillis = 1100, easing = FastOutSlowInEasing))
        else animated.snapTo(target)
    }
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val w = stroke.toPx()
            val topLeft = Offset(w / 2, w / 2)
            val arc = Size(size.width - w, size.height - w)
            drawArc(
                color = track, startAngle = 0f, sweepAngle = 360f, useCenter = false,
                topLeft = topLeft, size = arc, style = Stroke(width = w),
            )
            if (animated.value > 0.001f) {
                val brush: Brush = if (colours.size > 1) Brush.linearGradient(colours) else SolidColor(colours.first())
                drawArc(
                    brush = brush, startAngle = -90f, sweepAngle = 360f * animated.value, useCenter = false,
                    topLeft = topLeft, size = arc, style = Stroke(width = w, cap = StrokeCap.Round),
                )
            }
        }
        content()
    }
}

/** A slim gradient bar for progress through a scene. */
@Composable
fun GradientBar(fraction: Float, modifier: Modifier = Modifier) {
    val brand = LocalBrand.current
    Box(
        modifier = modifier
            .height(8.dp)
            .clip(PillShape)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .clip(PillShape)
                .background(Brush.horizontalGradient(brand.hero)),
        )
    }
}

// ── Decoration ────────────────────────────────────────────────────────────

/**
 * A fine-line azulejo tile pattern: a diamond and a dot in each tile, with
 * circles at the corners that join up into quatrefoils across the grid.
 */
@Composable
fun AzulejoPattern(modifier: Modifier = Modifier, colour: Color = Color.White.copy(alpha = 0.14f)) {
    Canvas(modifier) {
        val tile = 56.dp.toPx()
        val line = Stroke(width = 1.2.dp.toPx())
        var y = 0f
        while (y < size.height + tile) {
            var x = 0f
            while (x < size.width + tile) {
                val cx = x + tile / 2
                val cy = y + tile / 2
                val diamond = Path().apply {
                    moveTo(cx, y + tile * 0.16f)
                    lineTo(x + tile * 0.84f, cy)
                    lineTo(cx, y + tile * 0.84f)
                    lineTo(x + tile * 0.16f, cy)
                    close()
                }
                drawPath(path = diamond, color = colour, style = line)
                drawCircle(color = colour, radius = tile * 0.08f, center = Offset(cx, cy))
                drawCircle(color = colour, radius = tile * 0.30f, center = Offset(x, y), style = line)
                x += tile
            }
            y += tile
        }
    }
}

// ── Phrase text ───────────────────────────────────────────────────────────

/** Shows phrase markup with each numbered chunk in its link colour and in bold. */
@Composable
fun MarkedText(
    markup: String,
    gender: SpeakerGender,
    style: TextStyle,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
) {
    val colours = LocalChunkColours.current
    val text = remember(markup, gender, colours) {
        buildAnnotatedString {
            Markup.segments(markup, gender).forEach { segment ->
                if (segment.chunk in 1..colours.size) {
                    withStyle(SpanStyle(color = colours[segment.chunk - 1], fontWeight = FontWeight.Bold)) {
                        append(segment.text)
                    }
                } else {
                    append(segment.text)
                }
            }
        }
    }
    Text(text = text, style = style, modifier = modifier, textAlign = textAlign)
}

/** An emoji for each topic, keyed by the id prefix used in the CSV files. */
fun topicEmoji(topicKey: String): String = when (topicKey) {
    "greet" -> "👋"
    "hotel" -> "🏨"
    "food" -> "🥘"
    "around" -> "🚌"
    "shop" -> "🛍️"
    "health" -> "🩺"
    "travel" -> "✈️"
    "home" -> "🏠"
    "people" -> "👪"
    "work" -> "💼"
    "time" -> "⏰"
    "help" -> "🆘"
    "free" -> "🎨"
    "money" -> "💰"
    "tech" -> "📱"
    "talk" -> "💬"
    "nature" -> "🌿"
    "culture" -> "🎭"
    "errands" -> "📮"
    "stories" -> "📖"
    "news" -> "📰"
    "learn" -> "🎓"
    else -> "✨"
}
