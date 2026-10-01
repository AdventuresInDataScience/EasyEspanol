@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)

package com.example.easyespanol.ui.study

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.easyespanol.data.AppSettings
import com.example.easyespanol.data.CardFront
import com.example.easyespanol.data.Markup
import com.example.easyespanol.data.Phrase
import com.example.easyespanol.data.PhraseRepository
import com.example.easyespanol.data.Speaker
import com.example.easyespanol.navigation.Routes
import com.example.easyespanol.ui.common.AppCard
import com.example.easyespanol.ui.common.AppScaffold
import com.example.easyespanol.ui.common.GradientBar
import com.example.easyespanol.ui.common.GradientButton
import com.example.easyespanol.ui.common.MarkedText
import com.example.easyespanol.ui.common.ProgressRing
import com.example.easyespanol.ui.common.ChoiceButton
import com.example.easyespanol.ui.common.SpeechProblemBanner
import com.example.easyespanol.ui.common.Tag
import com.example.easyespanol.ui.common.TintButton
import com.example.easyespanol.ui.theme.LocalBrand
import com.example.easyespanol.ui.theme.PhraseTextStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StudyScreen(navController: NavController, repo: PhraseRepository, settings: AppSettings, speaker: Speaker, sceneKey: String) {
    val scene = repo.scene(settings.dialect, sceneKey)
    if (scene == null) {
        AppScaffold(title = "Scene", onBack = { navController.navigateUp() }) { padding ->
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("This scene isn't in the current phrase list. Go back and pick another.", textAlign = TextAlign.Center)
            }
        }
        return
    }

    LaunchedEffect(sceneKey) { settings.updateLastScene(sceneKey) }

    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // Take the list once per visit so cards don't vanish mid-session when marked as known.
    var includeKnown by rememberSaveable(sceneKey) { mutableStateOf(false) }
    val phrases = remember(sceneKey, settings.dialect, settings.maxGrammar, settings.maxVocab, settings.hideKnown, includeKnown) {
        scene.phrases.filter { phrase ->
            settings.isVisible(phrase) && (includeKnown || !settings.hideKnown || !settings.isKnown(phrase.id))
        }
    }
    // index == phrases.size means "end of scene".
    var index by rememberSaveable(sceneKey) { mutableStateOf(0) }
    val position = index.coerceIn(0, phrases.size)

    AppScaffold(title = scene.name, onBack = { navController.navigateUp() }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (phrases.isEmpty()) {
                EmptyScene(
                    allKnown = scene.phrases.any { settings.isVisible(it) },
                    onShowAll = { includeKnown = true; index = 0 },
                    onSettings = { navController.navigate(Routes.SETTINGS) },
                )
            } else {
                val progress by animateFloatAsState(position.toFloat() / phrases.size, tween(450), label = "progress")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GradientBar(progress, Modifier.weight(1f))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        if (position < phrases.size) "${position + 1} of ${phrases.size}" else "Done",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Cards slide in from the side you're moving towards.
                AnimatedContent(
                    targetState = position,
                    transitionSpec = {
                        val forward = targetState > initialState
                        val enter = slideInHorizontally(tween(320)) { width -> if (forward) width else -width } + fadeIn(tween(320))
                        val exit = slideOutHorizontally(tween(320)) { width -> if (forward) -width / 3 else width / 3 } + fadeOut(tween(200))
                        (enter togetherWith exit).using(SizeTransform(clip = false))
                    },
                    label = "card",
                ) { target ->
                    val phrase = phrases.getOrNull(target)
                    if (phrase != null) {
                        PhraseCard(phrase, settings)
                    } else {
                        val next = repo.nextScene(settings.dialect, sceneKey)
                        SceneFinished(
                            known = settings.knownCount(phrases),
                            total = phrases.size,
                            nextSceneName = next?.name,
                            onNextScene = {
                                if (next != null) {
                                    navController.navigate(Routes.study(next.key)) {
                                        popUpTo(Routes.STUDY) { inclusive = true }
                                    }
                                }
                            },
                            onAgain = { index = 0 },
                            onBack = { navController.navigateUp() },
                        )
                    }
                }

                val current = phrases.getOrNull(position)
                if (current != null) {
                    Controls(
                        canGoBack = position > 0,
                        isKnown = settings.isKnown(current.id),
                        onSpeak = { speaker.speak(Markup.plain(current.spanish, settings.gender), settings.dialect) },
                        onPrevious = { if (position > 0) index = position - 1 },
                        onNext = { index = position + 1 },
                        // Mark, let the ring show for a moment, then move on.
                        onKnown = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            settings.setKnown(current.id, true)
                            settings.recordReview()
                            val next = position + 1
                            scope.launch { delay(260); index = next }
                        },
                        onLearning = {
                            settings.setKnown(current.id, false)
                            settings.recordReview()
                            val next = position + 1
                            scope.launch { delay(260); index = next }
                        },
                    )
                    speaker.problem?.let { message ->
                        SpeechProblemBanner(
                            message = message,
                            onInstall = { speaker.openVoiceSettings() },
                            onDismiss = { speaker.clearProblem() },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhraseCard(phrase: Phrase, settings: AppSettings) {
    var revealed by remember { mutableStateOf(false) }
    val brand = LocalBrand.current
    val spanishFirst = settings.cardFront == CardFront.SPANISH
    val front = if (spanishFirst) phrase.spanish else phrase.english
    val back = if (spanishFirst) phrase.english else phrase.spanish
    val frontName = if (spanishFirst) "Spanish" else "English"
    val backName = if (spanishFirst) "English" else "Spanish"

    AppCard(
        modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp),
        onClick = { revealed = !revealed },
        padding = 24.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Tag(frontName, MaterialTheme.colorScheme.primary)
            Spacer(Modifier.weight(1f))
            Tag("Grammar ${phrase.grammar}", brand.accents[4])
            Spacer(Modifier.width(6.dp))
            Tag("Vocab ${phrase.vocab}", brand.accents[5])
        }
        Spacer(Modifier.height(24.dp))
        MarkedText(front, settings.gender, PhraseTextStyle)

        // The answer grows into place when the card is tapped.
        AnimatedVisibility(
            visible = revealed,
            enter = expandVertically(tween(320)) + fadeIn(tween(320)),
            exit = shrinkVertically(tween(240)) + fadeOut(tween(200)),
        ) {
            Column {
                Spacer(Modifier.height(22.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(Modifier.height(22.dp))
                Tag(backName, MaterialTheme.colorScheme.tertiary)
                Spacer(Modifier.height(14.dp))
                MarkedText(back, settings.gender, PhraseTextStyle)
                if (phrase.note.isNotBlank()) {
                    Spacer(Modifier.height(18.dp))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(brand.accents[2].copy(alpha = if (brand.dark) 0.16f else 0.10f))
                            .padding(14.dp),
                    ) {
                        Text("💡", modifier = Modifier.padding(end = 10.dp))
                        Text(phrase.note, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        if (!revealed) {
            Spacer(Modifier.height(28.dp))
            Text(
                "Tap to see the $backName",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun Controls(
    canGoBack: Boolean,
    isKnown: Boolean,
    onSpeak: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onKnown: () -> Unit,
    onLearning: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilledTonalIconButton(onClick = onPrevious, enabled = canGoBack, modifier = Modifier.size(56.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous phrase")
        }
        TintButton("🔊  Listen", onSpeak, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
        FilledTonalIconButton(onClick = onNext, modifier = Modifier.size(56.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next phrase")
        }
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ChoiceButton("Still learning", selected = !isKnown, onClick = onLearning, modifier = Modifier.weight(1f))
        ChoiceButton("I know this", selected = isKnown, onClick = onKnown, modifier = Modifier.weight(1f))
    }
    Text(
        if (isKnown) "Marked as known. Tap Still learning to change it."
        else "Matching colours link each part of the Spanish to the English.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SceneFinished(
    known: Int,
    total: Int,
    nextSceneName: String?,
    onNextScene: () -> Unit,
    onAgain: () -> Unit,
    onBack: () -> Unit,
) {
    val brand = LocalBrand.current
    AppCard(modifier = Modifier.fillMaxWidth(), padding = 28.dp) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ProgressRing(
                progress = if (total == 0) 0f else known.toFloat() / total,
                modifier = Modifier.size(132.dp),
                stroke = 12.dp,
                colours = brand.hero,
                track = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
            ) {
                Text("🎉", fontSize = 44.sp)
            }
            Text("¡Muy bien!", style = MaterialTheme.typography.displaySmall)
            Text(
                "You know $known of the $total phrases in this scene.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            if (nextSceneName != null) {
                GradientButton("Next: $nextSceneName", onNextScene, Modifier.fillMaxWidth())
            }
            TintButton("Go through again", onAgain, MaterialTheme.colorScheme.primary, Modifier.fillMaxWidth())
            TextButton(onClick = onBack) { Text("Back to scenes") }
        }
    }
}

@Composable
private fun EmptyScene(allKnown: Boolean, onShowAll: () -> Unit, onSettings: () -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), padding = 28.dp) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(if (allKnown) "⭐" else "🎚️", fontSize = 44.sp)
            if (allKnown) {
                Text("You know every phrase here", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                GradientButton("Practise them anyway", onShowAll, Modifier.fillMaxWidth())
            } else {
                Text("No phrases here match your levels", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                GradientButton("Change levels", onSettings, Modifier.fillMaxWidth())
            }
        }
    }
}
