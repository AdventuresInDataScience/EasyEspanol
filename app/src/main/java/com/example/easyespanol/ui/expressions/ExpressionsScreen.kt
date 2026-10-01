package com.example.easyespanol.ui.expressions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.easyespanol.data.AppSettings
import com.example.easyespanol.data.Expression
import com.example.easyespanol.data.Markup
import com.example.easyespanol.data.PhraseRepository
import com.example.easyespanol.data.Speaker
import com.example.easyespanol.ui.common.BackTopBar
import com.example.easyespanol.ui.common.LevelBadge
import com.example.easyespanol.ui.common.MarkedText

@Composable
fun ExpressionsScreen(navController: NavController, repo: PhraseRepository, settings: AppSettings) {
    val groups = repo.expressions(settings.dialect).groupBy { it.group }
    val context = LocalContext.current
    val speaker = remember { Speaker(context.applicationContext) }
    DisposableEffect(Unit) { onDispose { speaker.shutdown() } }

    Scaffold(topBar = { BackTopBar("Colloquial expressions") { navController.navigateUp() } }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "intro") {
                Text(
                    "Everyday sayings from ${settings.dialect.label}. The colours link each part " +
                        "of the Spanish to its word-for-word translation.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            groups.forEach { (group, expressions) ->
                item(key = "group-$group") {
                    Text(
                        group,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                items(expressions, key = { it.id }) { expression ->
                    ExpressionCard(expression, settings, speechReady = speaker.ready) {
                        speaker.speak(Markup.plain(expression.spanish, settings.gender), settings.dialect)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpressionCard(expression: Expression, settings: AppSettings, speechReady: Boolean, onSpeak: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MarkedText(
                    expression.spanish,
                    settings.gender,
                    MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                if (expression.register.isNotBlank()) LevelBadge(expression.register)
            }
            if (expression.literal.isNotBlank()) {
                Row {
                    Text("Literally: ", style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
                    MarkedText(expression.literal, settings.gender, MaterialTheme.typography.bodyMedium)
                }
            }
            Text(expression.meaning, style = MaterialTheme.typography.bodyLarge)
            if (expression.note.isNotBlank()) {
                Text(
                    "💡 ${expression.note}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onSpeak, enabled = speechReady) { Text("🔊  Listen") }
        }
    }
}
