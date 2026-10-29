package com.example.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class VoiceEngine(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var isMuted = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale("es", "ES"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale("es"))
                }
                tts?.setSpeechRate(1.0f)
                tts?.setPitch(1.0f)
                isInitialized = true
                Log.d("VoiceEngine", "TTS initialized successfully")
            } else {
                Log.w("VoiceEngine", "TTS initialization failed: $status")
            }
        }
    }

    fun setMuted(muted: Boolean) {
        this.isMuted = muted
        if (muted) {
            stop()
        }
    }

    fun isMuted(): Boolean = isMuted

    fun speak(text: String, flush: Boolean = true) {
        if (isMuted || !isInitialized || text.isBlank()) return
        try {
            // Eliminar caracteres como '#' (que el TTS lee como 'almohadilla') y emojis decorativos
            val sanitized = text
                .replace("#", " ")
                .replace("🔴", "")
                .replace("🩵", "")
                .replace("🇦🇷", "")
                .replace("🇺🇲", "")
                .replace("➔", " ")
                .trim()
            if (sanitized.isBlank()) return
            val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            val utteranceId = "vpn_voice_${System.currentTimeMillis()}"
            tts?.speak(sanitized, queueMode, null, utteranceId)
        } catch (e: Exception) {
            Log.e("VoiceEngine", "Error speaking text", e)
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e("VoiceEngine", "Error stopping TTS", e)
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("VoiceEngine", "Error shutting down TTS", e)
        }
    }
}
