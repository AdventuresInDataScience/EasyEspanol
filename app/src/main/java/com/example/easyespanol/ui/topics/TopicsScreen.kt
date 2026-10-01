package com.example.easyespanol.ui.topics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.easyespanol.data.AppSettings
import com.example.easyespanol.data.PhraseRepository
import com.example.easyespanol.navigation.Routes
import com.example.easyespanol.ui.common.AppScaffold
import com.example.easyespanol.ui.common.EmojiTile
import com.example.easyespanol.ui.common.ProgressRow
import com.example.easyespanol.ui.common.topicEmoji
import com.example.easyespanol.ui.theme.LocalBrand

@Composable
fun TopicsScreen(navController: NavController, repo: PhraseRepository, settings: AppSettings) {
    val brand = LocalBrand.current
    val topics = repo.topics(settings.dialect)
    AppScaffold(title = "Topics", onBack = { navController.navigateUp() }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp, end = 20.dp,
                top = padding.calculateTopPadding() + 4.dp,
                bottom = padding.calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "intro") {
                Text(
                    "Each topic is split into short scenes. Start anywhere.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            itemsIndexed(topics, key = { _, topic -> topic.key }) { index, topic ->
                val phrases = topic.phrases.filter { settings.isVisible(it) }
                ProgressRow(
                    emoji = topicEmoji(topic.key),
                    colour = brand.accents[index % brand.accents.size],
                    title = topic.name,
                    subtitle = "${topic.scenes.size} scenes, ${phrases.size} phrases",
                    known = settings.knownCount(phrases),
                    total = phrases.size,
                    onClick = { navController.navigate(Routes.scenes(topic.key)) },
                )
            }
        }
    }
}

@Composable
fun ScenesScreen(navController: NavController, repo: PhraseRepository, settings: AppSettings, topicKey: String) {
    val brand = LocalBrand.current
    val topics = repo.topics(settings.dialect)
    val topicIndex = topics.indexOfFirst { it.key == topicKey }
    val topic = topics.getOrNull(topicIndex)
    val colour = brand.accents[(if (topicIndex < 0) 0 else topicIndex) % brand.accents.size]

    AppScaffold(title = topic?.name ?: "Scenes", onBack = { navController.navigateUp() }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp, end = 20.dp,
                top = padding.calculateTopPadding() + 4.dp,
                bottom = padding.calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (topic != null) {
                item(key = "intro") {
                    val phrases = topic.phrases.filter { settings.isVisible(it) }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                        EmojiTile(topicEmoji(topic.key), colour, 64.dp)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("${topic.scenes.size} scenes", style = MaterialTheme.typography.titleLarge)
                            Text(
                                "${settings.knownCount(phrases)} of ${phrases.size} phrases known",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            itemsIndexed(topic?.scenes ?: emptyList(), key = { _, scene -> scene.key }) { _, scene ->
                val phrases = scene.phrases.filter { settings.isVisible(it) }
                ProgressRow(
                    emoji = scene.name.take(1).uppercase(),
                    monogram = true,
                    colour = colour,
                    title = scene.name,
                    subtitle = if (phrases.isEmpty()) "Nothing at your chosen levels" else "${phrases.size} phrases",
                    known = settings.knownCount(phrases),
                    total = phrases.size,
                    enabled = phrases.isNotEmpty(),
                    onClick = { navController.navigate(Routes.study(scene.key)) },
                )
            }
        }
    }
}
