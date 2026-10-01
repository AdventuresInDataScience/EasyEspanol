package com.example.easyespanol.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.easyespanol.data.AppSettings
import com.example.easyespanol.data.PhraseRepository
import com.example.easyespanol.navigation.Routes
import com.example.easyespanol.ui.common.AppCard
import com.example.easyespanol.ui.common.AppScaffold
import com.example.easyespanol.ui.common.AzulejoPattern
import com.example.easyespanol.ui.common.EmojiTile
import com.example.easyespanol.ui.common.GradientButton
import com.example.easyespanol.ui.common.PillShape
import com.example.easyespanol.ui.common.ProgressRing
import com.example.easyespanol.ui.common.percentText
import com.example.easyespanol.ui.theme.LocalBrand

@Composable
fun HomeScreen(navController: NavController, repo: PhraseRepository, settings: AppSettings) {
    val brand = LocalBrand.current
    val topics = repo.topics(settings.dialect)
    val allPhrases = remember(topics) { topics.flatMap { it.phrases } }
    val known = settings.knownCount(allPhrases)
    val lastScene = settings.lastScene?.let { repo.scene(settings.dialect, it) }

    AppScaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HeroTile(
                place = "${settings.dialect.flag}  ${settings.dialect.label}",
                known = known,
                total = allPhrases.size,
                onSettings = { navController.navigate(Routes.SETTINGS) },
            )

            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatTile("🔥", "${settings.streak}", if (settings.streak == 1) "day in a row" else "days in a row", brand.accents[2], Modifier.weight(1f).fillMaxHeight())
                StatTile("✅", "${settings.reviewedToday}", "cards today", brand.known, Modifier.weight(1f).fillMaxHeight())
                StatTile("🧠", "$known", "phrases known", brand.accents[0], Modifier.weight(1f).fillMaxHeight())
            }

            if (lastScene != null) {
                GradientButton(
                    text = "Continue: ${lastScene.name}",
                    onClick = { navController.navigate(Routes.study(lastScene.key)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                GradientButton(
                    text = "Start your first scene",
                    onClick = { navController.navigate(Routes.TOPICS) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ActionTile(
                    emoji = "📚", title = "Topics", subtitle = "${topics.size} topics, from greetings to the news",
                    colour = brand.accents[0], modifier = Modifier.weight(1f).fillMaxHeight(),
                ) { navController.navigate(Routes.TOPICS) }
                ActionTile(
                    emoji = "💬", title = "Expressions", subtitle = "Sayings people really use",
                    colour = brand.accents[1], modifier = Modifier.weight(1f).fillMaxHeight(),
                ) { navController.navigate(Routes.EXPRESSIONS) }
            }
        }
    }
}

/** The signature tile: sunset gradient, azulejo pattern, greeting and overall progress. */
@Composable
private fun HeroTile(place: String, known: Int, total: Int, onSettings: () -> Unit) {
    val brand = LocalBrand.current
    val shape = RoundedCornerShape(32.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(18.dp, shape, ambientColor = brand.hero.first(), spotColor = brand.hero.last())
            .clip(shape)
            .background(Brush.linearGradient(brand.hero)),
    ) {
        AzulejoPattern(Modifier.matchParentSize())
        Column(Modifier.padding(start = 22.dp, end = 14.dp, top = 14.dp, bottom = 24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    place,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    modifier = Modifier
                        .clip(PillShape)
                        .background(Color.White.copy(alpha = 0.18f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color.White)
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("¡Hola!", style = MaterialTheme.typography.displayMedium, color = Color.White)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "You know ${"%,d".format(known)} of ${"%,d".format(total)} phrases.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                }
                Spacer(Modifier.width(12.dp))
                ProgressRing(
                    progress = if (total == 0) 0f else known.toFloat() / total,
                    modifier = Modifier.size(96.dp),
                    stroke = 10.dp,
                    colours = listOf(Color.White),
                    track = Color.White.copy(alpha = 0.22f),
                ) {
                    Text(percentText(known, total), style = MaterialTheme.typography.titleLarge, color = Color.White)
                }
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}

@Composable
private fun StatTile(emoji: String, value: String, label: String, colour: Color, modifier: Modifier) {
    AppCard(modifier = modifier, padding = 14.dp) {
        EmojiTile(emoji, colour, 36.dp)
        Spacer(Modifier.height(10.dp))
        Text(value, style = MaterialTheme.typography.headlineMedium)
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ActionTile(
    emoji: String,
    title: String,
    subtitle: String,
    colour: Color,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    AppCard(modifier = modifier, onClick = onClick) {
        EmojiTile(emoji, colour, 48.dp)
        Spacer(Modifier.height(14.dp))
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(2.dp))
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
