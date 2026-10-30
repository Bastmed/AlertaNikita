package com.bastian.macrobast.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class AlertManager(private val context: Context) {

    private val sharedPrefs = context.getSharedPreferences("MacroBastPrefs", Context.MODE_PRIVATE)
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun reproducirAlarmaStandard() {
        reproducirSonidoPersonalizado("sound_standard", RingtoneManager.TYPE_ALARM, AudioAttributes.USAGE_ALARM, AudioManager.STREAM_ALARM, 4000L)
        vibrar(4000L)
    }

    fun reproducirAlarmaPromo() {
        reproducirSonidoPersonalizado("sound_promo", RingtoneManager.TYPE_RINGTONE, AudioAttributes.USAGE_NOTIFICATION_RINGTONE, AudioManager.STREAM_RING, 3500L)
        val patron = longArrayOf(0, 200, 200, 200, 200, 200, 200, 200)
        vibrarPatron(patron)
    }

    fun reproducirAlarmaArena() {
        reproducirSonidoPersonalizado("sound_arena", RingtoneManager.TYPE_ALARM, AudioAttributes.USAGE_ALARM, AudioManager.STREAM_ALARM, 3500L)
        val patron = longArrayOf(0, 150, 100, 150, 500, 150, 100, 150)
        vibrarPatron(patron)
    }

    fun reproducirAviso() {
        reproducirSonidoPersonalizado("sound_aviso", RingtoneManager.TYPE_NOTIFICATION, AudioAttributes.USAGE_NOTIFICATION, AudioManager.STREAM_NOTIFICATION, 2000L)
        vibrar(500L)
    }

    private fun maximizarVolumen(streamType: Int) {
        try {
            // Obtiene el volumen máximo permitido por el fabricante para ese canal específico
            val volumenMaximo = audioManager.getStreamMaxVolume(streamType)

            // Establece el volumen al máximo. El '0' al final evita que salga
            // la barra visual de volumen en la pantalla para no estorbar la interfaz.
            audioManager.setStreamVolume(streamType, volumenMaximo, 0)
        } catch (e: Exception) {
            Log.e("AlertManager", "Error al maximizar volumen: ${e.message}")
        }
    }

    private fun reproducirSonidoPersonalizado(prefKey: String, defaultType: Int, usoAudio: Int, streamType: Int, duracionMillis: Long) {
        try {
            val savedUriString = sharedPrefs.getString(prefKey, "")
            val uri = if (savedUriString.isNullOrEmpty()) RingtoneManager.getDefaultUri(defaultType)
            else Uri.parse(savedUriString)

            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(context, uri)

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(usoAudio)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            mediaPlayer.setAudioAttributes(audioAttributes)
            mediaPlayer.prepare()

            // SE DISPARA EL VOLUMEN AL 100% JUSTO ANTES DE REPRODUCIR
            maximizarVolumen(streamType)

            mediaPlayer.start()

            Handler(Looper.getMainLooper()).postDelayed({
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                    mediaPlayer.release()
                }
            }, duracionMillis)
        } catch (e: Exception) {
            Log.e("AlertManager", "Error al reproducir audio personalizado: ${e.message}")
        }
    }

    private fun vibrar(duracion: Long) {
        val vibrator = obtenerVibrador()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duracion, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duracion)
        }
    }

    private fun vibrarPatron(patron: LongArray) {
        val vibrator = obtenerVibrador()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(patron, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(patron, -1)
        }
    }

    private fun obtenerVibrador(): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
}