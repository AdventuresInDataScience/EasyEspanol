package com.example.easyespanol.data

import android.content.Context

/** Loads the phrase and expression CSVs from app/src/main/assets, once per dialect. */
class PhraseRepository(private val context: Context) {

    private val topicCache = mutableMapOf<Dialect, List<Topic>>()
    private val expressionCache = mutableMapOf<Dialect, List<Expression>>()

    fun topics(dialect: Dialect): List<Topic> =
        topicCache.getOrPut(dialect) { loadTopics(dialect) }

    fun topic(dialect: Dialect, key: String): Topic? =
        topics(dialect).find { it.key == key }

    fun scene(dialect: Dialect, key: String): Scene? =
        topic(dialect, key.substringBefore('-'))?.scenes?.find { it.key == key }

    /** The scene after this one in the same topic, or null if it's the last. */
    fun nextScene(dialect: Dialect, key: String): Scene? {
        val scenes = topic(dialect, key.substringBefore('-'))?.scenes ?: return null
        val index = scenes.indexOfFirst { it.key == key }
        return if (index >= 0 && index + 1 < scenes.size) scenes[index + 1] else null
    }

    fun expressions(dialect: Dialect): List<Expression> =
        expressionCache.getOrPut(dialect) {
            Csv.parse(readAsset(dialect.expressionsFile)).map { r ->
                Expression(
                    id = r["id"].orEmpty(),
                    group = r["group"].orEmpty(),
                    register = r["register"].orEmpty(),
                    spanish = r["spanish"].orEmpty(),
                    literal = r["literal"].orEmpty(),
                    meaning = r["meaning"].orEmpty(),
                    note = r["note"].orEmpty(),
                )
            }
        }

    private fun loadTopics(dialect: Dialect): List<Topic> {
        val phrases = Csv.parse(readAsset(dialect.phrasesFile)).map { r ->
            val id = r["id"].orEmpty()
            Phrase(
                id = id,
                topicKey = id.substringBefore('-'),
                sceneKey = id.substringBeforeLast('-'),
                topic = r["topic"].orEmpty(),
                scene = r["scene"].orEmpty(),
                grammar = r["grammar"]?.toIntOrNull() ?: 1,
                vocab = r["vocab"]?.toIntOrNull() ?: 1,
                spanish = r["spanish"].orEmpty(),
                english = r["english"].orEmpty(),
                note = r["note"].orEmpty(),
            )
        }
        // groupBy keeps the order rows appear in the file.
        return phrases.groupBy { it.topicKey }.map { (topicKey, topicPhrases) ->
            val scenes = topicPhrases.groupBy { it.sceneKey }.map { (sceneKey, scenePhrases) ->
                Scene(sceneKey, scenePhrases.first().scene, scenePhrases)
            }
            Topic(topicKey, topicPhrases.first().topic, scenes)
        }
    }

    private fun readAsset(name: String): String =
        context.assets.open(name).bufferedReader(Charsets.UTF_8).use { it.readText() }
}
