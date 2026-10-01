package com.example.easyespanol.data

/** Which phrase files to load. Both files share ids, so progress carries across. */
enum class Dialect(
    val label: String,
    val flag: String,
    val phrasesFile: String,
    val expressionsFile: String,
    val speechRegion: String,
) {
    SPAIN("Spain", "🇪🇸", "phrases_spain.csv", "expressions_spain.csv", "ES"),
    MEXICO("Mexico", "🇲🇽", "phrases_latam.csv", "expressions_latam.csv", "MX"),
}

/** Words describing the speaker change form, e.g. cansad[o|a] becomes cansado or cansada. */
enum class SpeakerGender(val label: String) {
    MALE("Male"),
    FEMALE("Female"),
}

/** Which side of the card is shown first. */
enum class CardFront(val label: String, val description: String) {
    ENGLISH("English first", "See the English, try to say it in Spanish"),
    SPANISH("Spanish first", "Read the Spanish, work out what it means"),
}

data class Phrase(
    val id: String,
    val topicKey: String,
    val sceneKey: String,
    val topic: String,
    val scene: String,
    val grammar: Int,
    val vocab: Int,
    val spanish: String,
    val english: String,
    val note: String,
)

data class Scene(val key: String, val name: String, val phrases: List<Phrase>)

data class Topic(val key: String, val name: String, val scenes: List<Scene>) {
    val phrases: List<Phrase> get() = scenes.flatMap { it.phrases }
}

data class Expression(
    val id: String,
    val group: String,
    val register: String,
    val spanish: String,
    val literal: String,
    val meaning: String,
    val note: String,
)
