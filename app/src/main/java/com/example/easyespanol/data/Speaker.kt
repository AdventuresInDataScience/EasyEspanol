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

/**
 * Reads Spanish aloud with the phone's text-to-speech engine.
 *
 * Text-to-speech fails silently in several common situations, so this class
 * checks for each one and reports it in `problem`, in words a learner can act on:
 *  - no Spanish voice installed (most phones only come with their own language)
 *  - the phone's default engine (e.g. Samsung's) has no Spanish, but Google's does:
 *    it switches to Google's engine automatically
 *  - the voice is still downloading, or needs the internet
 *  - media volume turned all the way down
 *
 * One Speaker is created when the app starts and shared by every screen.
 */
class Speaker(private val context: Context) {

    var status: SpeechStatus by mutableStateOf(SpeechStatus.STARTING)
        private set

    /** A message to show the learner when something stopped speech, or null. */
    var problem: String? by mutableStateOf(null)
        private set

    /** Name of the engine in use, shown in Settings. */
    var engineName: String by mutableStateOf("")
        private set

    private val mainHandler = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech? = null
    private var triedGoogle = false
    private var pending: Pair<String, Dialect>? = null

    init {
        start(null)
    }

    private fun start(enginePackage: String?) {
        tts?.shutdown()
        status = SpeechStatus.STARTING
        // Always handle the result on the main thread, after `tts` has been assigned:
        // some engines report failure from inside the constructor.
        val listener = TextToSpeech.OnInitListener { result -> mainHandler.post { onInit(result) } }
        tts = if (enginePackage == null) TextToSpeech(context, listener)
        else TextToSpeech(context, listener, enginePackage)
    }

    private fun onInit(result: Int) {
        val engine = tts
        if (result != TextToSpeech.SUCCESS || engine == null) {
            if (switchToGoogle()) return
            status = SpeechStatus.NO_ENGINE
            return
        }
        engineName = engine.engines.firstOrNull { it.name == engine.defaultEngine }?.label ?: ""
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
        if (!hasSpanish(engine)) {
            if (switchToGoogle()) return
            status = SpeechStatus.NO_SPANISH_VOICE
            return
        }
        status = SpeechStatus.READY
        pending?.let { (text, dialect) ->
            pending = null
            speak(text, dialect)
        }
    }

    /** If the current engine can't do Spanish and Google's engine is installed, use that instead. */
    private fun switchToGoogle(): Boolean {
        if (triedGoogle) return false
        triedGoogle = true
        val current = tts ?: return false
        val hasGoogle = current.engines.any { it.name == GOOGLE_ENGINE }
        if (!hasGoogle || current.defaultEngine == GOOGLE_ENGINE) return false
        start(GOOGLE_ENGINE)
        return true
    }

    fun speak(text: String, dialect: Dialect) {
        problem = null
        val engine = tts
        if (status == SpeechStatus.STARTING) {
            pending = text to dialect // spoken as soon as the engine is ready
            return
        }
        if (engine == null || status == SpeechStatus.NO_ENGINE) {
            problem = "This phone has no text-to-speech engine. Install \"Speech Services by Google\" from the Play Store."
            return
        }
        if (status == SpeechStatus.NO_SPANISH_VOICE) {
            // The learner may have installed a voice since we last checked.
            if (hasSpanish(engine)) status = SpeechStatus.READY
            else {
                problem = "No Spanish voice is installed on this phone yet. Tap Install voice, add Spanish, then try again."
                return
            }
        }
        if (mediaVolumeIsZero()) {
            problem = "Your media volume is turned all the way down. Turn it up with the volume buttons."
        }

        val locale = bestLocale(engine, dialect)
        engine.setLanguage(locale)
        bestVoice(engine, dialect)?.let { engine.setVoice(it) }
        val result = engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "phrase-${System.nanoTime()}")
        if (result == TextToSpeech.ERROR) problem = messageFor(TextToSpeech.ERROR)
    }

    /** Plays a short sample, used by the "Test voice" button in Settings. */
    fun test(dialect: Dialect) = speak("Hola, ¿qué tal? Vamos a practicar.", dialect)

    /** Opens the phone's screen for installing voices (or the nearest settings screen). */
    fun openVoiceSettings() {
        val candidates = listOf(
            Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA),
            Intent("com.android.settings.TTS_SETTINGS"),
            Intent(Settings.ACTION_SETTINGS),
        )
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

    private fun isAvailable(engine: TextToSpeech, locale: Locale): Boolean =
        engine.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE

    private fun hasSpanish(engine: TextToSpeech): Boolean =
        listOf("ES", "MX", "US", null).any { isAvailable(engine, spanish(it)) }

    /** Spanish for the chosen country if the phone has it, otherwise the closest Spanish it has. */
    private fun bestLocale(engine: TextToSpeech, dialect: Dialect): Locale {
        val order = if (dialect == Dialect.MEXICO) listOf("MX", "US", "ES", null) else listOf("ES", "MX", "US", null)
        return order.map { spanish(it) }.firstOrNull { isAvailable(engine, it) } ?: spanish(null)
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
            "The phone's speech engine couldn't read this out. Tap Install voice and check that Spanish is installed."
    }

    private companion object {
        const val GOOGLE_ENGINE = "com.google.android.tts"
    }
}
