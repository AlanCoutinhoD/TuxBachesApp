package com.example.tuxbaches.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject

private const val TTS_CHECK_CODE = 1234

class VoiceAlertManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private lateinit var tts: TextToSpeech
    private var isReady = false
    private val pendingMessages = mutableListOf<Pair<Int, String>>()
    private val readyListeners = mutableListOf<() -> Unit>()

    init {
        initializeTts()
    }

    private fun initializeTts() {
        tts = TextToSpeech(context) { status ->
            when (status) {
                TextToSpeech.SUCCESS -> {
                    val spanishLocale = Locale("es", "ES")
                    when (tts.setLanguage(spanishLocale)) {
                        TextToSpeech.LANG_MISSING_DATA -> {
                            println("Paquete de idioma español no instalado")
                            // Sugerir instalación
                            installLanguageData(spanishLocale)
                        }
                        TextToSpeech.LANG_NOT_SUPPORTED -> {
                            println("Idioma español no soportado")
                            tts.setLanguage(Locale.US) // Fallback a inglés
                        }
                        else -> {
                            isReady = true
                            readyListeners.forEach { it() }
                            processPendingMessages()
                        }
                    }
                }
                TextToSpeech.ERROR -> {
                    println("TTS initialization failed - please install TTS engine")
                    // Remove the activity-related code since we can't use it with application context
                    // Instead, just log the error and let the user know they need to install TTS
                }
                else -> {
                    println("Unknown TTS initialization status: $status")
                }
            }
        }
    }

    private fun installLanguageData(locale: Locale) {
        val installIntent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
        installIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            context.startActivity(installIntent)
        } catch (e: Exception) {
            println("No se pudo iniciar la instalación: ${e.message}")
        }
    }

    private fun processPendingMessages() {
        pendingMessages.forEach { (distance, type) ->
            speakNow(distance, type)
        }
        pendingMessages.clear()
    }

    fun addOnReadyListener(listener: () -> Unit) {
        if (isReady) listener() else readyListeners.add(listener)
    }

    fun speakIncidentAlert(distance: Int, title: String) {  // Cambiado de 'type' a 'title'
        if (!isReady) {
            pendingMessages.add(distance to title)  // Cambiado de 'type' a 'title'
            println("Queuing message - TTS not ready yet")
            return
        }
        speakNow(distance, title)  // Cambiado de 'type' a 'title'
    }

    private fun speakNow(distance: Int, title: String) {  // Cambiado de 'type' a 'title'
        try {
            val message = if (distance == 0) title else "Atención: A $distance metros hay $title. Tome precauciones."  // Cambiado de 'type' a 'title'
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
        } catch (e: Exception) {
            println("TTS speak error: ${e.message}")
        }
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
        pendingMessages.clear()
    }
}