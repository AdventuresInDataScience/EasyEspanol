package com.example.easyespanol.ui.settings

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.easyespanol.data.AppSettings
import com.example.easyespanol.data.CardFront
import com.example.easyespanol.data.Dialect
import com.example.easyespanol.data.PhraseRepository
import com.example.easyespanol.data.Speaker
import com.example.easyespanol.data.SpeakerGender
import com.example.easyespanol.data.SpeechStatus
import com.example.easyespanol.ui.common.AppCard
import com.example.easyespanol.ui.common.AppScaffold
import com.example.easyespanol.ui.common.PillShape
import com.example.easyespanol.ui.common.SpeechProblemBanner
import com.example.easyespanol.ui.common.TintButton
import com.example.easyespanol.ui.theme.AppPalette
import com.example.easyespanol.ui.theme.ThemeMode
import com.example.easyespanol.ui.theme.brandFor

@Composable
fun SettingsScreen(navController: NavController, repo: PhraseRepository, settings: AppSettings, speaker: Speaker) {
    var confirmReset by remember { mutableStateOf(false) }

    AppScaffold(title = "Settings", onBack = { navController.navigateUp() }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Section("Your Spanish") {
                SettingLabel("Variety", "The phrases are the same; the wording changes where Spain and Mexico differ.")
                Segmented(Dialect.entries, settings.dialect, { "${it.flag}  ${it.label}" }) { settings.updateDialect(it) }
                Spacer(Modifier.height(18.dp))
                SettingLabel("I'm…", "Phrases about you use the matching form, such as cansado or cansada.")
                Segmented(SpeakerGender.entries, settings.gender, { it.label }) { settings.updateGender(it) }
                Spacer(Modifier.height(18.dp))
                SettingLabel("Card front", settings.cardFront.description)
                Segmented(CardFront.entries, settings.cardFront, { it.label }) { settings.updateCardFront(it) }
            }

            Section("Difficulty") {
                SettingLabel("Grammar", "1: present tense and set phrases. 2: past, future and commands. 3: subjunctive and 'if' sentences.")
                Segmented(listOf(1, 2, 3), settings.maxGrammar, ::levelLabel) { settings.updateMaxGrammar(it) }
                Spacer(Modifier.height(18.dp))
                SettingLabel("Vocabulary", "1: core words. 2: everyday words. 3: more specific words.")
                Segmented(listOf(1, 2, 3), settings.maxVocab, ::levelLabel) { settings.updateMaxVocab(it) }
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(role = Role.Switch) { settings.updateHideKnown(!settings.hideKnown) }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Skip phrases I know", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Scenes show only what you're still learning",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(checked = settings.hideKnown, onCheckedChange = { settings.updateHideKnown(it) })
                }
            }

            Section("Speech") {
                val status = when (speaker.status) {
                    SpeechStatus.STARTING -> "Starting up…"
                    SpeechStatus.READY -> if (speaker.engineLabel.isBlank()) "Ready" else "Ready, using ${speaker.engineLabel}"
                    SpeechStatus.NO_SPANISH_VOICE -> "No Spanish voice found in ${speaker.engineLabel}"
                    SpeechStatus.NO_ENGINE -> "No speech engine is working"
                }
                SettingLabel("Listen button", status)
                if (speaker.engines.size > 1) {
                    Text(
                        "Speech engine",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text(
                        "Your phone has more than one. If Spanish won't play, pick the one you installed Spanish in.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    speaker.engines.forEach { engine ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(role = Role.RadioButton) {
                                    settings.updateTtsEngine(engine.name)
                                    speaker.useEngine(engine.name)
                                }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = engine.name == speaker.engineName,
                                onClick = {
                                    settings.updateTtsEngine(engine.name)
                                    speaker.useEngine(engine.name)
                                },
                            )
                            Text(engine.label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TintButton("Test voice", { speaker.test(settings.dialect) }, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                    TintButton("Install voice", { speaker.openVoiceSettings() }, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
                }
                TextButton(onClick = { speaker.openSpeechSettings() }) { Text("Open the phone's speech settings") }
                if (speaker.report.isNotBlank()) {
                    Text(
                        speaker.report,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                speaker.problem?.let { message ->
                    Spacer(Modifier.height(12.dp))
                    SpeechProblemBanner(message, { speaker.openVoiceSettings() }, { speaker.clearProblem() })
                }
            }

            Section("Look and feel") {
                SettingLabel("Colour scheme", settings.palette.description)
                PalettePicker(settings.palette) { settings.updatePalette(it) }
                Spacer(Modifier.height(18.dp))
                SettingLabel("Light or dark", null)
                Segmented(ThemeMode.entries, settings.themeMode, { it.label }) { settings.updateThemeMode(it) }
            }

            Section("Progress") {
                SettingLabel("Start again", "Marks every phrase as not yet known. Your settings stay as they are.")
                TintButton("Reset progress", { confirmReset = true }, MaterialTheme.colorScheme.error, Modifier.fillMaxWidth())
            }

            val topics = repo.topics(settings.dialect)
            Text(
                "Easy Español, ${topics.sumOf { it.phrases.size }} phrases in ${topics.size} topics",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            )
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset progress?") },
            text = { Text("Every phrase will be marked as not yet known. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = { settings.resetProgress(); confirmReset = false }) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) { Text("Cancel") }
            },
        )
    }
}

private fun levelLabel(level: Int): String = when (level) {
    1 -> "Level 1"
    2 -> "Up to 2"
    else -> "All"
}

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth(), padding = 20.dp) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(14.dp))
        content()
    }
}

@Composable
private fun SettingLabel(title: String, description: String?) {
    Text(title, style = MaterialTheme.typography.titleMedium)
    if (description != null) {
        Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Spacer(Modifier.height(10.dp))
}

/** A row of pill options; the chosen one fills with the primary colour. */
@Composable
private fun <T> Segmented(options: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(PillShape)
            .background(scheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(4.dp),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            val background by animateColorAsState(if (isSelected) scheme.primary else Color.Transparent, label = "segment")
            val textColour by animateColorAsState(if (isSelected) scheme.onPrimary else scheme.onSurfaceVariant, label = "segmentText")
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(PillShape)
                    .background(background)
                    .clickable(role = Role.RadioButton) { onSelect(option) }
                    .padding(vertical = 11.dp, horizontal = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(label(option), style = MaterialTheme.typography.labelLarge, color = textColour, maxLines = 1)
            }
        }
    }
}

/** Gradient swatches for each palette. The chosen one gets a ring. */
@Composable
private fun PalettePicker(selected: AppPalette, onSelect: (AppPalette) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val dark = com.example.easyespanol.ui.theme.LocalBrand.current.dark
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AppPalette.entries.forEach { palette ->
            val available = palette != AppPalette.WALLPAPER || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            val brush = if (palette == AppPalette.WALLPAPER) {
                Brush.sweepGradient(listOf(Color(0xFFE53935), Color(0xFFFDD835), Color(0xFF43A047), Color(0xFF1E88E5), Color(0xFF8E24AA), Color(0xFFE53935)))
            } else {
                Brush.linearGradient(brandFor(palette, dark, scheme).hero)
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(76.dp)
                    .alpha(if (available) 1f else 0.4f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(enabled = available, role = Role.RadioButton) { onSelect(palette) }
                    .padding(vertical = 6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .border(3.dp, if (palette == selected) scheme.onSurface else Color.Transparent, CircleShape)
                        .padding(6.dp)
                        .clip(CircleShape)
                        .background(brush),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    if (palette == AppPalette.WALLPAPER) "Wallpaper" else palette.label,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
            }
        }
    }
}
