package com.example.easyespanol.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.easyespanol.data.AppSettings
import com.example.easyespanol.data.PhraseRepository
import com.example.easyespanol.navigation.Routes
import com.example.easyespanol.ui.common.ProgressLine
import com.example.easyespanol.ui.common.SpanishFlag

@Composable
fun HomeScreen(navController: NavController, repo: PhraseRepository, settings: AppSettings) {
    val topics = repo.topics(settings.dialect)
    val allPhrases = topics.flatMap { it.phrases }
    val known = settings.knownCount(allPhrases)
    val lastScene = settings.lastScene?.let { repo.scene(settings.dialect, it) }
    val buttonModifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SpanishFlag(Modifier.width(180.dp).height(120.dp))
            Spacer(Modifier.height(24.dp))
            Text("Easy Español", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                "${settings.dialect.flag}  Spanish as spoken in ${settings.dialect.label}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Phrases you know",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(6.dp))
            ProgressLine(known, allPhrases.size)
            Spacer(Modifier.height(32.dp))

            if (lastScene != null) {
                Button(onClick = { navController.navigate(Routes.study(lastScene.key)) }, modifier = buttonModifier) {
                    Text("▶  Continue: ${lastScene.name}", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(12.dp))
                FilledTonalButton(onClick = { navController.navigate(Routes.TOPICS) }, modifier = buttonModifier) {
                    Text("📚  All topics", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                Button(onClick = { navController.navigate(Routes.TOPICS) }, modifier = buttonModifier) {
                    Text("📚  Start learning", style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(Modifier.height(12.dp))
            FilledTonalButton(onClick = { navController.navigate(Routes.EXPRESSIONS) }, modifier = buttonModifier) {
                Text("💬  Colloquial expressions", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = { navController.navigate(Routes.SETTINGS) }, modifier = buttonModifier) {
                Text("⚙️  Settings", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
