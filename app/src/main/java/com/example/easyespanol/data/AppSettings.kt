package com.example.easyespanol.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.easyespanol.ui.theme.AppPalette
import com.example.easyespanol.ui.theme.ThemeMode
import java.time.LocalDate

/**
 * The learner's settings and progress, saved on the phone with SharedPreferences.
 * Every value is Compose state, so screens update as soon as something changes.
 * Progress is stored against the permanent phrase ids, so it survives dialect
 * switches and content updates.
 */
class AppSettings(context: Context) {

    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val progressPrefs = context.getSharedPreferences("progress", Context.MODE_PRIVATE)

    var dialect: Dialect by mutableStateOf(enumPref("dialect", Dialect.SPAIN))
        private set
    var gender: SpeakerGender by mutableStateOf(enumPref("gender", SpeakerGender.MALE))
        private set
    var cardFront: CardFront by mutableStateOf(enumPref("card_front", CardFront.ENGLISH))
        private set
    var palette: AppPalette by mutableStateOf(enumPref("palette", AppPalette.ATARDECER))
        private set
    var themeMode: ThemeMode by mutableStateOf(enumPref("theme_mode", ThemeMode.SYSTEM))
        private set
    var maxGrammar: Int by mutableStateOf(prefs.getInt("max_grammar", 3))
        private set
    var maxVocab: Int by mutableStateOf(prefs.getInt("max_vocab", 3))
        private set
    var hideKnown: Boolean by mutableStateOf(prefs.getBoolean("hide_known", false))
        private set
    var lastScene: String? by mutableStateOf<String?>(prefs.getString("last_scene", null))
        private set

    fun updateDialect(value: Dialect) { dialect = value; saveEnum("dialect", value) }
    fun updateGender(value: SpeakerGender) { gender = value; saveEnum("gender", value) }
    fun updateCardFront(value: CardFront) { cardFront = value; saveEnum("card_front", value) }
    fun updatePalette(value: AppPalette) { palette = value; saveEnum("palette", value) }
    fun updateThemeMode(value: ThemeMode) { themeMode = value; saveEnum("theme_mode", value) }

    fun updateMaxGrammar(value: Int) {
        maxGrammar = value
        prefs.edit().putInt("max_grammar", value).apply()
    }

    fun updateMaxVocab(value: Int) {
        maxVocab = value
        prefs.edit().putInt("max_vocab", value).apply()
    }

    fun updateHideKnown(value: Boolean) {
        hideKnown = value
        prefs.edit().putBoolean("hide_known", value).apply()
    }

    fun updateLastScene(value: String) {
        lastScene = value
        prefs.edit().putString("last_scene", value).apply()
    }

    /** True if the phrase is within the learner's chosen grammar and vocabulary levels. */
    fun isVisible(phrase: Phrase): Boolean =
        phrase.grammar <= maxGrammar && phrase.vocab <= maxVocab

    // ── Daily streak and today's count (shown on the home screen) ───────────

    private var lastStudyDay: Long by mutableStateOf(prefs.getLong("last_study_day", Long.MIN_VALUE / 2))
    private var streakDays: Int by mutableStateOf(prefs.getInt("streak", 0))
    private var reviewedDay: Long by mutableStateOf(prefs.getLong("reviewed_day", Long.MIN_VALUE / 2))
    private var reviewedCount: Int by mutableStateOf(prefs.getInt("reviewed_count", 0))

    /** Days in a row with at least one card reviewed. Still counts until today ends. */
    val streak: Int get() = if (lastStudyDay >= today() - 1) streakDays else 0

    val reviewedToday: Int get() = if (reviewedDay == today()) reviewedCount else 0

    /** Call whenever a card is marked known or still learning. */
    fun recordReview() {
        val day = today()
        streakDays = when (lastStudyDay) {
            day -> maxOf(streakDays, 1)
            day - 1 -> streakDays + 1
            else -> 1
        }
        reviewedCount = if (reviewedDay == day) reviewedCount + 1 else 1
        reviewedDay = day
        lastStudyDay = day
        prefs.edit()
            .putLong("last_study_day", day)
            .putInt("streak", streakDays)
            .putLong("reviewed_day", day)
            .putInt("reviewed_count", reviewedCount)
            .apply()
    }

    private fun today(): Long = LocalDate.now().toEpochDay()

    // ── Progress ────────────────────────────────────────────────────────────

    private val known = mutableStateMapOf<String, Boolean>().apply {
        progressPrefs.all.forEach { (id, value) -> if (value == true) put(id, true) }
    }

    fun isKnown(id: String): Boolean = known[id] == true

    fun knownCount(phrases: List<Phrase>): Int = phrases.count { isKnown(it.id) }

    fun setKnown(id: String, value: Boolean) {
        val editor = progressPrefs.edit()
        if (value) {
            known[id] = true
            editor.putBoolean(id, true)
        } else {
            known.remove(id)
            editor.remove(id)
        }
        editor.apply()
    }

    fun resetProgress() {
        known.clear()
        progressPrefs.edit().clear().apply()
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private inline fun <reified T : Enum<T>> enumPref(key: String, default: T): T {
        val name = prefs.getString(key, null) ?: return default
        return enumValues<T>().firstOrNull { it.name == name } ?: default
    }

    private fun saveEnum(key: String, value: Enum<*>) {
        prefs.edit().putString(key, value.name).apply()
    }
}
