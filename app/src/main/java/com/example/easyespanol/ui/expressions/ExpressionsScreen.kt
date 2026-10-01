package com.example.easyespanol.ui.expressions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.easyespanol.data.AppSettings
import com.example.easyespanol.data.Expression
import com.example.easyespanol.data.Markup
import com.example.easyespanol.data.PhraseRepository
import com.example.easyespanol.data.Speaker
import com.example.easyespanol.ui.common.AppCard
import com.example.easyespanol.ui.common.AppScaffold
import com.example.easyespanol.ui.common.MarkedText
import com.example.easyespanol.ui.common.SpeechProblemBanner
import com.example.easyespanol.ui.common.Tag
import com.example.easyespanol.ui.common.TintButton
import com.example.easyespanol.ui.theme.LocalBrand
import com.example.easyespanol.ui.theme.PhraseTextStyle

@Composable
fun ExpressionsScreen(navController: NavController, repo: PhraseRepository, settings: AppSettings, speaker: Speaker) {
    val groups = repo.expressions(settings.dialect).groupBy { it.group }

    AppScaffold(title = "Expressions", onBack = { navController.navigateUp() }) { padding ->
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
                    "Everyday sayings from ${settings.dialect.label}. The colours link the Spanish to a word-for-word translation.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            speaker.problem?.let { message ->
                item(key = "speech-problem") {
                    SpeechProblemBanner(
                        message = message,
                        onInstall = { speaker.openVoiceSettings() },
                        onDismiss = { speaker.clearProblem() },
                    )
                }
            }
            groups.forEach { (group, expressions) ->
                item(key = "group-$group") {
                    Text(group, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 12.dp))
                }
                items(expressions, key = { it.id }) { expression ->
                    ExpressionCard(expression, settings) {
                        speaker.speak(Markup.plain(expression.spanish, settings.gender), settings.dialect)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpressionCard(expression: Expression, settings: AppSettings, onSpeak: () -> Unit) {
    val brand = LocalBrand.current
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            MarkedText(
                expression.spanish,
                settings.gender,
                PhraseTextStyle.copy(fontSize = PhraseTextStyle.fontSize * 0.85f, lineHeight = PhraseTextStyle.lineHeight * 0.85f),
                modifier = Modifier.weight(1f),
            )
            if (expression.register.isNotBlank()) {
                Spacer(Modifier.width(8.dp))
                Tag(expression.register.replaceFirstChar { it.uppercase() }, registerColour(expression.register, brand.accents))
            }
        }
        if (expression.literal.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Row {
                Text("Word for word: ", style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                MarkedText(expression.literal, settings.gender, MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(expression.meaning, style = MaterialTheme.typography.titleMedium)
        if (expression.note.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(expression.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(12.dp))
        TintButton("🔊  Listen", onSpeak, MaterialTheme.colorScheme.primary)
    }
}

/** Slang stands out most; neutral expressions are the calmest colour. */
private fun registerColour(register: String, accents: List<Color>): Color = when (register.lowercase()) {
    "slang" -> accents[1]
    "informal" -> accents[2]
    else -> accents[0]
}
