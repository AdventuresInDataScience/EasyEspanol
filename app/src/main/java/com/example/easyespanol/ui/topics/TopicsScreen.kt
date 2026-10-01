package com.example.easyespanol.ui.topics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.easyespanol.data.AppSettings
import com.example.easyespanol.data.PhraseRepository
import com.example.easyespanol.navigation.Routes
import com.example.easyespanol.ui.common.BackTopBar
import com.example.easyespanol.ui.common.ListCard

@Composable
fun TopicsScreen(navController: NavController, repo: PhraseRepository, settings: AppSettings) {
    val topics = repo.topics(settings.dialect)
    Scaffold(topBar = { BackTopBar("Topics") { navController.navigateUp() } }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(topics, key = { it.key }) { topic ->
                val phrases = topic.phrases.filter { settings.isVisible(it) }
                ListCard(
                    title = topic.name,
                    subtitle = "${topic.scenes.size} scenes · ${phrases.size} phrases",
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
    val topic = repo.topic(settings.dialect, topicKey)
    Scaffold(topBar = { BackTopBar(topic?.name ?: "Scenes") { navController.navigateUp() } }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(topic?.scenes ?: emptyList(), key = { it.key }) { scene ->
                val phrases = scene.phrases.filter { settings.isVisible(it) }
                ListCard(
                    title = scene.name,
                    subtitle = if (phrases.isEmpty()) "No phrases at your chosen levels"
                    else "${phrases.size} phrases",
                    known = settings.knownCount(phrases),
                    total = phrases.size,
                    enabled = phrases.isNotEmpty(),
                    onClick = { navController.navigate(Routes.study(scene.key)) },
                )
            }
        }
    }
}
