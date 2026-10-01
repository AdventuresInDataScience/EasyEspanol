@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.easyespanol.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.easyespanol.data.Markup
import com.example.easyespanol.data.SpeakerGender
import com.example.easyespanol.ui.theme.FlagGold
import com.example.easyespanol.ui.theme.FlagRed
import com.example.easyespanol.ui.theme.LocalChunkColours

/** Top bar with a back arrow, used on every screen except home. */
@Composable
fun BackTopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    )
}

/** The red-gold-red flag, drawn to whatever size the modifier gives it. */
@Composable
fun SpanishFlag(modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(16.dp))) {
        Box(Modifier.fillMaxWidth().weight(1f).background(FlagRed))
        Box(Modifier.fillMaxWidth().weight(2f).background(FlagGold))
        Box(Modifier.fillMaxWidth().weight(1f).background(FlagRed))
    }
}

/** Shows phrase markup with each numbered chunk in its link colour. */
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
                    withStyle(SpanStyle(color = colours[segment.chunk - 1], fontWeight = FontWeight.SemiBold)) {
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

/** A thin progress bar with "known / total" beside it. */
@Composable
fun ProgressLine(known: Int, total: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        LinearProgressIndicator(
            progress = { if (total == 0) 0f else known.toFloat() / total },
            modifier = Modifier.weight(1f).height(6.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            "$known / $total",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** A tappable card used for topics and scenes. */
@Composable
fun ListCard(
    title: String,
    subtitle: String,
    known: Int,
    total: Int,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Card(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (total > 0) {
                Spacer(Modifier.height(12.dp))
                ProgressLine(known, total)
            }
        }
    }
}

/** A small rounded label, e.g. "Grammar 2". */
@Composable
fun LevelBadge(text: String) {
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.secondaryContainer) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}
