package com.example.easyespanol.data

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/** Reads Spanish aloud with the phone's text-to-speech engine. */
class Speaker(context: Context) {

    var ready: Boolean by mutableStateOf(false)
        private set

    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context) { status ->
            ready = status == TextToSpeech.SUCCESS
        }
    }

    fun speak(text: String, dialect: Dialect) {
        val engine = tts ?: return
        val regional = Locale.Builder().setLanguage("es").setRegion(dialect.speechRegion).build()
        val result = engine.setLanguage(regional)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            // No voice for that country installed: fall back to any Spanish voice.
            engine.setLanguage(Locale.Builder().setLanguage("es").build())
        }
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "easy-espanol")
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
