@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.easyespanol.ui.study

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.easyespanol.data.AppSettings
import com.example.easyespanol.data.CardFront
import com.example.easyespanol.data.Markup
import com.example.easyespanol.data.Phrase
import com.example.easyespanol.data.PhraseRepository
import com.example.easyespanol.data.Speaker
import com.example.easyespanol.navigation.Routes
import com.example.easyespanol.ui.common.BackTopBar
import com.example.easyespanol.ui.common.LevelBadge
import com.example.easyespanol.ui.common.MarkedText

@Composable
fun StudyScreen(navController: NavController, repo: PhraseRepository, settings: AppSettings, sceneKey: String) {
    val scene = repo.scene(settings.dialect, sceneKey)
    if (scene == null) {
        Scaffold(topBar = { BackTopBar("Scene") { navController.navigateUp() } }) { padding ->
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("This scene couldn't be found.")
            }
        }
        return
    }

    LaunchedEffect(sceneKey) { settings.updateLastScene(sceneKey) }

    val context = LocalContext.current
    val speaker = remember { Speaker(context.applicationContext) }
    DisposableEffect(Unit) { onDispose { speaker.shutdown() } }

    // Take the list once per visit so cards don't disappear mid-session when marked as known.
    var includeKnown by rememberSaveable(sceneKey) { mutableStateOf(false) }
    val phrases = remember(sceneKey, settings.dialect, settings.maxGrammar, settings.maxVocab, settings.hideKnown, includeKnown) {
        scene.phrases.filter { phrase ->
            settings.isVisible(phrase) && (includeKnown || !settings.hideKnown || !settings.isKnown(phrase.id))
        }
    }
    // index == phrases.size means "end of scene".
    var index by rememberSaveable(sceneKey) { mutableStateOf(0) }
    val position = index.coerceIn(0, phrases.size)

    Scaffold(topBar = { BackTopBar(scene.name) { navController.navigateUp() } }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when {
                phrases.isEmpty() -> EmptyScene(
                    allKnown = scene.phrases.any { settings.isVisible(it) },
                    onShowAll = { includeKnown = true; index = 0 },
                    onSettings = { navController.navigate(Routes.SETTINGS) },
                )

                position >= phrases.size -> SceneFinished(
                    known = settings.knownCount(phrases),
                    total = phrases.size,
                    onAgain = { index = 0 },
                    nextSceneName = repo.nextScene(settings.dialect, sceneKey)?.name,
                    onNextScene = {
                        repo.nextScene(settings.dialect, sceneKey)?.let { next ->
                            navController.navigate(Routes.study(next.key)) {
                                popUpTo(Routes.STUDY) { inclusive = true }
                            }
                        }
                    },
                    onBack = { navController.navigateUp() },
                )

                else -> {
                    val phrase = phrases[position]
                    // key() gives each phrase its own "revealed" state, starting hidden.
                    key(phrase.id) {
                        PhraseCard(
                            phrase = phrase,
                            position = position + 1,
                            total = phrases.size,
                            settings = settings,
                            speechReady = speaker.ready,
                            onSpeak = { speaker.speak(Markup.plain(phrase.spanish, settings.gender), settings.dialect) },
                            onPrevious = { if (position > 0) index = position - 1 },
                            onNext = { index = position + 1 },
                            onKnown = { settings.setKnown(phrase.id, true); index = position + 1 },
                            onLearning = { settings.setKnown(phrase.id, false); index = position + 1 },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhraseCard(
    phrase: Phrase,
    position: Int,
    total: Int,
    settings: AppSettings,
    speechReady: Boolean,
    onSpeak: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onKnown: () -> Unit,
    onLearning: () -> Unit,
) {
    var revealed by remember { mutableStateOf(false) }
    val spanishFirst = settings.cardFront == CardFront.SPANISH
    val front = if (spanishFirst) phrase.spanish else phrase.english
    val back = if (spanishFirst) phrase.english else phrase.spanish

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("$position / $total", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            LevelBadge("Grammar ${phrase.grammar}")
            LevelBadge("Vocab ${phrase.vocab}")
        }
    }

    Card(onClick = { revealed = !revealed }, modifier = Modifier.fillMaxWidth().heightIn(min = 260.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SideLabel(if (spanishFirst) "SPANISH" else "ENGLISH")
            MarkedText(front, settings.gender, MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            if (revealed) {
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))
                SideLabel(if (spanishFirst) "ENGLISH" else "SPANISH")
                MarkedText(back, settings.gender, MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                if (phrase.note.isNotBlank()) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "💡 ${phrase.note}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                Text(
                    "Tap to reveal",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(onClick = onPrevious, enabled = position > 1) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous phrase")
        }
        FilledTonalButton(onClick = onSpeak, enabled = speechReady) {
            Text("🔊  Listen")
        }
        OutlinedButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next phrase")
        }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = onLearning,
            modifier = Modifier.weight(1f).heightIn(min = 52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ),
        ) { Text("✗  Still learning") }
        Button(onClick = onKnown, modifier = Modifier.weight(1f).heightIn(min = 52.dp)) {
            Text("✓  I know this")
        }
    }

    Text(
        if (settings.isKnown(phrase.id)) "You've marked this phrase as known."
        else "Matching colours show which Spanish words go with which English words.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun SideLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun SceneFinished(
    known: Int,
    total: Int,
    onAgain: () -> Unit,
    nextSceneName: String?,
    onNextScene: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("🎉", style = MaterialTheme.typography.displayMedium)
        Text("¡Muy bien! Scene finished", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "You know $known of the $total phrases here.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        if (nextSceneName != null) {
            Button(onClick = onNextScene, modifier = Modifier.fillMaxWidth()) { Text("Next scene: $nextSceneName") }
        }
        FilledTonalButton(onClick = onAgain, modifier = Modifier.fillMaxWidth()) { Text("Go through again") }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back to scenes") }
    }
}

@Composable
private fun EmptyScene(allKnown: Boolean, onShowAll: () -> Unit, onSettings: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (allKnown) {
            Text("You know every phrase in this scene.", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Button(onClick = onShowAll) { Text("Practise them anyway") }
        } else {
            Text("No phrases here match your level settings.", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Button(onClick = onSettings) { Text("Change levels in Settings") }
        }
    }
}
