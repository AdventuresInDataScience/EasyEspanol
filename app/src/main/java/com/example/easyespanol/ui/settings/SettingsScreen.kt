package com.example.easyespanol.ui.settings

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.easyespanol.data.AppSettings
import com.example.easyespanol.data.CardFront
import com.example.easyespanol.data.Dialect
import com.example.easyespanol.data.PhraseRepository
import com.example.easyespanol.data.SpeakerGender
import com.example.easyespanol.ui.common.BackTopBar
import com.example.easyespanol.ui.theme.AppPalette
import com.example.easyespanol.ui.theme.ThemeMode

private val levelNames = mapOf(1 to "Level 1 only", 2 to "Up to 2", 3 to "All levels")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, repo: PhraseRepository, settings: AppSettings) {
    var confirmReset by remember { mutableStateOf(false) }

    Scaffold(topBar = { BackTopBar("Settings") { navController.navigateUp() } }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Section("Spanish variety", "Both have the same phrases; the wording changes where Spain and Mexico differ.") {
                Dialect.entries.forEach { d ->
                    Choice("${d.flag}  ${d.label}", null, settings.dialect == d) { settings.updateDialect(d) }
                }
            }

            Section("I am…", "Some phrases change with who's speaking, e.g. cansado / cansada.") {
                SpeakerGender.entries.forEach { g ->
                    Choice(g.label, null, settings.gender == g) { settings.updateGender(g) }
                }
            }

            Section("Card front", null) {
                CardFront.entries.forEach { f ->
                    Choice(f.label, f.description, settings.cardFront == f) { settings.updateCardFront(f) }
                }
            }

            Section("Grammar", "1: present tense and set phrases · 2: past, future and commands · 3: subjunctive and 'if' sentences") {
                LevelChips(settings.maxGrammar) { settings.updateMaxGrammar(it) }
            }

            Section("Vocabulary", "1: core words · 2: everyday words · 3: more specific words") {
                LevelChips(settings.maxVocab) { settings.updateMaxVocab(it) }
            }

            Section("Practice", null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { settings.updateHideKnown(!settings.hideKnown) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Skip phrases I know", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Only show phrases you haven't marked as known",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(checked = settings.hideKnown, onCheckedChange = { settings.updateHideKnown(it) })
                }
            }

            Section("Colour scheme", null) {
                AppPalette.entries.forEach { p ->
                    val unavailable = p == AppPalette.WALLPAPER && Build.VERSION.SDK_INT < Build.VERSION_CODES.S
                    Choice(
                        label = p.label,
                        description = if (unavailable) "Needs Android 12 or newer" else p.description,
                        selected = settings.palette == p,
                        enabled = !unavailable,
                        trailing = { Swatches(p) },
                    ) { settings.updatePalette(p) }
                }
            }

            Section("Light or dark", null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { m ->
                        FilterChip(
                            selected = settings.themeMode == m,
                            onClick = { settings.updateThemeMode(m) },
                            label = { Text(m.label) },
                        )
                    }
                }
            }

            Section("Progress", null) {
                OutlinedButton(
                    onClick = { confirmReset = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Reset all progress") }
            }

            val topics = repo.topics(settings.dialect)
            Text(
                "Easy Español · ${topics.sumOf { it.phrases.size }} phrases in ${topics.size} topics",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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

@Composable
private fun Section(title: String, description: String?, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        if (description != null) {
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun Choice(
    label: String,
    description: String?,
    selected: Boolean,
    enabled: Boolean = true,
    trailing: @Composable () -> Unit = {},
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick, enabled = enabled)
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            if (description != null) {
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        trailing()
    }
}

@Composable
private fun Swatches(palette: AppPalette) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(end = 8.dp)) {
        palette.swatch.forEach { colour ->
            Box(Modifier.size(20.dp).clip(CircleShape).background(colour))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LevelChips(selected: Int, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        (1..3).forEach { level ->
            FilterChip(
                selected = selected == level,
                onClick = { onSelect(level) },
                label = { Text(levelNames.getValue(level)) },
            )
        }
    }
}
