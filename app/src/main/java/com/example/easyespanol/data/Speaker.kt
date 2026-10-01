package com.example.easyespanol.data

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

enum class SpeechStatus { STARTING, READY, NO_SPANISH_VOICE, NO_ENGINE }

/** A text-to-speech engine installed on the phone, e.g. Google's or Samsung's. */
data class EngineChoice(val name: String, val label: String)

/**
 * Reads Spanish aloud with the phone's text-to-speech engine.
 *
 * Lessons from real phones (the emulator hides all of these):
 *  - Engines can wrongly report "no Spanish" for a moment after starting up, so
 *    nothing is decided at start-up: Spanish is chosen when Listen is tapped.
 *  - A voice installed while the app is open often isn't seen until the engine
 *    restarts, so a failed attempt restarts it once and tries again, and the app
 *    restarts it when the learner comes back from the phone's settings.
 *  - Phones can have several engines (Samsung phones usually have Samsung's and
 *    Google's). Spanish may be installed in one but not the other, so the
 *    learner can choose the engine in Settings, and Install voice opens the
 *    installer of the engine actually in use.
 *
 * One Speaker is created when the app starts and shared by every screen.
 */
class Speaker(private val context: Context, preferredEngine: String?) {

    var status: SpeechStatus by mutableStateOf(SpeechStatus.STARTING)
        private set

    /** A message to show the learner when speech couldn't play, or null. */
    var problem: String? by mutableStateOf(null)
        private set

    /** The engine in use (package name and readable name). */
    var engineName: String? by mutableStateOf<String?>(null)
        private set
    var engineLabel: String by mutableStateOf("")
        private set

    /** Every speech engine on the phone, for the picker in Settings. */
    var engines: List<EngineChoice> by mutableStateOf(emptyList())
        private set

    /** What the engine says about Spanish, shown in Settings to help diagnose problems. */
    var report: String by mutableStateOf("")
        private set

    private val mainHandler = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech? = null
    private var generation = 0
    private var requested: String? = preferredEngine
    private var restartedForRetry = false
    private var triedGoogle = false
    private var pending: Pair<String, Dialect>? = null

    init {
        start(preferredEngine)
    }

    /** Switch to another engine (from Settings). null means the phone's default. */
    fun useEngine(name: String?) {
        requested = name
        restartedForRetry = false
        triedGoogle = false
        problem = null
        start(name)
    }

    /** Called when the app comes back to the front: picks up newly installed voices. */
    fun refresh() {
        if (status == SpeechStatus.STARTING) return
        if (problem != null || status != SpeechStatus.READY) {
            restartedForRetry = false
            triedGoogle = false
            problem = null
            start(requested)
        }
    }

    private fun start(enginePackage: String?) {
        tts?.shutdown()
        status = SpeechStatus.STARTING
        val myGeneration = ++generation
        // Handle the result on the main thread, and ignore results from an engine
        // we've since replaced (some engines also report failure from inside the constructor).
        val listener = TextToSpeech.OnInitListener { result ->
            mainHandler.post { if (myGeneration == generation) onInit(result, enginePackage) }
        }
        tts = if (enginePackage == null) TextToSpeech(context, listener)
        else TextToSpeech(context, listener, enginePackage)
    }

    private fun onInit(result: Int, enginePackage: String?) {
        val engine = tts
        if (result != TextToSpeech.SUCCESS || engine == null) {
            status = SpeechStatus.NO_ENGINE
            if (pending != null) {
                pending = null
                problem = "The phone's speech engine wouldn't start. Choose another engine in Settings, or install \"Speech Services by Google\" from the Play Store."
            }
            return
        }
        val inUse = enginePackage ?: engine.defaultEngine
        engines = engine.engines.map { EngineChoice(it.name, it.label) }
        engineName = inUse
        engineLabel = engines.firstOrNull { it.name == inUse }?.label ?: (inUse ?: "")
        engine.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {}

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                mainHandler.post { problem = messageFor(TextToSpeech.ERROR) }
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                mainHandler.post { problem = messageFor(errorCode) }
            }
        })
        status = SpeechStatus.READY
        updateReport()
        pending?.let { (text, dialect) ->
            pending = null
            speak(text, dialect)
        }
    }

    fun speak(text: String, dialect: Dialect) {
        problem = null
        if (status == SpeechStatus.STARTING) {
            pending = text to dialect // spoken as soon as the engine is ready
            return
        }
        val engine = tts
        if (engine == null || status == SpeechStatus.NO_ENGINE) {
            problem = "No speech engine is working on this phone. Choose another engine in Settings, or install \"Speech Services by Google\" from the Play Store."
            return
        }

        if (!selectSpanish(engine, dialect)) {
            when {
                // A voice installed since start-up is often only seen after a restart.
                !restartedForRetry -> {
                    restartedForRetry = true
                    pending = text to dialect
                    start(engineName ?: requested)
                }
                // This engine really has no Spanish; Google's engine might.
                !triedGoogle && engineName != GOOGLE_ENGINE && engines.any { it.name == GOOGLE_ENGINE } -> {
                    triedGoogle = true
                    pending = text to dialect
                    start(GOOGLE_ENGINE)
                }
                else -> {
                    status = SpeechStatus.NO_SPANISH_VOICE
                    updateReport()
                    problem = "$engineLabel says it has no Spanish voice. Tap Install voice to add Spanish to it, " +
                        "or choose another speech engine in Settings, under Speech."
                }
            }
            return
        }

        restartedForRetry = false
        status = SpeechStatus.READY
        if (mediaVolumeIsZero()) {
            problem = "Your media volume is turned all the way down. Turn it up with the volume buttons."
        }
        val result = engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "phrase-${System.nanoTime()}")
        if (result == TextToSpeech.ERROR) problem = messageFor(TextToSpeech.ERROR)
    }

    /** Plays a short sample, used by the "Test voice" button in Settings. */
    fun test(dialect: Dialect) = speak("Hola, ¿qué tal? Vamos a practicar.", dialect)

    /** Opens the voice installer of the engine in use (or the nearest settings screen). */
    fun openVoiceSettings() {
        val candidates = mutableListOf<Intent>()
        engineName?.let { candidates.add(Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA).setPackage(it)) }
        candidates.add(Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA))
        candidates.add(Intent("com.android.settings.TTS_SETTINGS"))
        candidates.add(Intent(Settings.ACTION_SETTINGS))
        open(candidates)
    }

    /** Opens the phone's text-to-speech settings, where the preferred engine is chosen. */
    fun openSpeechSettings() {
        open(listOf(Intent("com.android.settings.TTS_SETTINGS"), Intent(Settings.ACTION_SETTINGS)))
    }

    private fun open(candidates: List<Intent>) {
        for (intent in candidates) {
            try {
                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                return
            } catch (e: ActivityNotFoundException) {
                // try the next one
            } catch (e: SecurityException) {
                // try the next one
            }
        }
    }

    fun clearProblem() {
        problem = null
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private fun spanish(region: String?): Locale {
        val builder = Locale.Builder().setLanguage("es")
        if (region != null) builder.setRegion(region)
        return builder.build()
    }

    /** Points the engine at Spanish: an installed Spanish voice if it lists one, otherwise by language. */
    private fun selectSpanish(engine: TextToSpeech, dialect: Dialect): Boolean {
        bestVoice(engine, dialect)?.let { voice ->
            if (engine.setVoice(voice) == TextToSpeech.SUCCESS) return true
        }
        val order = if (dialect == Dialect.MEXICO) listOf("MX", "US", "ES", null) else listOf("ES", "MX", "US", null)
        for (region in order) {
            val result = try {
                engine.setLanguage(spanish(region))
            } catch (e: Exception) {
                TextToSpeech.LANG_NOT_SUPPORTED
            }
            if (result >= TextToSpeech.LANG_AVAILABLE) return true
        }
        return false
    }

    /** An installed Spanish voice, preferring the right country and ones that work offline. */
    private fun bestVoice(engine: TextToSpeech, dialect: Dialect): Voice? {
        val voices = try {
            engine.voices
        } catch (e: Exception) {
            null
        } ?: return null
        return voices
            .filter { voice ->
                voice.locale.language == "es" &&
                    voice.features?.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED) != true
            }
            .sortedWith(
                compareByDescending<Voice> { it.locale.country == dialect.speechRegion }
                    .thenBy { it.isNetworkConnectionRequired }
                    .thenByDescending { it.quality }
            )
            .firstOrNull()
    }

    private fun updateReport() {
        val engine = tts ?: return
        fun describe(region: String): String {
            val code = try {
                engine.isLanguageAvailable(spanish(region))
            } catch (e: Exception) {
                TextToSpeech.LANG_NOT_SUPPORTED
            }
            return when {
                code >= TextToSpeech.LANG_AVAILABLE -> "available"
                code == TextToSpeech.LANG_MISSING_DATA -> "not downloaded"
                else -> "not supported"
            }
        }
        val voiceCount = try {
            engine.voices?.count { voice ->
                voice.locale.language == "es" &&
                    voice.features?.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED) != true
            } ?: 0
        } catch (e: Exception) {
            0
        }
        report = "$engineLabel reports Spanish (Spain): ${describe("ES")}, Spanish (Mexico): ${describe("MX")}, " +
            "installed Spanish voices: $voiceCount."
    }

    private fun mediaVolumeIsZero(): Boolean {
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return false
        return audio.getStreamVolume(AudioManager.STREAM_MUSIC) == 0
    }

    private fun messageFor(errorCode: Int): String = when (errorCode) {
        TextToSpeech.ERROR_NOT_INSTALLED_YET ->
            "The Spanish voice is still downloading. Try again in a minute."
        TextToSpeech.ERROR_NETWORK, TextToSpeech.ERROR_NETWORK_TIMEOUT ->
            "This voice needs the internet. Connect, or tap Install voice to download Spanish for offline use."
        TextToSpeech.ERROR_OUTPUT ->
            "The phone couldn't play the sound. Check that no other app is using the speaker."
        else ->
            "$engineLabel couldn't read this out. Try another speech engine in Settings, under Speech."
    }

    private companion object {
        const val GOOGLE_ENGINE = "com.google.android.tts"
    }
}
