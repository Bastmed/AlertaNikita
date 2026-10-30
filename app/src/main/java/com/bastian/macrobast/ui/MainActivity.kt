package com.bastian.macrobast.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.bastian.macrobast.R

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPrefs: SharedPreferences
    private var mediaPlayerPreview: MediaPlayer? = null

    data class SoundOption(val name: String, val uri: String) {
        override fun toString(): String = name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // BOTONES DE PERMISOS
        val btnNotificaciones = findViewById<Button>(R.id.btnPermisoNotificaciones)
        val btnBateria = findViewById<Button>(R.id.btnPermisoBateria)
        val btnSobreponer = findViewById<Button>(R.id.btnPermisoSobreponer)

        btnNotificaciones.setOnClickListener {
            // Abre la pantalla para permitir leer notificaciones
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        btnBateria.setOnClickListener {
            // Abre la información de la aplicación para cambiar la batería a "Sin restricciones"
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }

        btnSobreponer.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                try {
                    // Intenta abrir directamente la configuración de tu app
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                } catch (e: Exception) {
                    // Si el teléfono bloquea el acceso directo, abre la lista general
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    startActivity(intent)
                }
            } else {
                // Si ya está activo, muestra un mensaje en pantalla
                android.widget.Toast.makeText(this, "Este permiso ya está activado", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        // Si es la primera vez y no tiene el permiso principal, lo enviamos directo (Tu lógica original)
        if (!isNotificationServiceEnabled()) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        // CONFIGURACIÓN DE SONIDOS
        sharedPrefs = getSharedPreferences("MacroBastPrefs", Context.MODE_PRIVATE)

        val spinnerStandard = findViewById<Spinner>(R.id.spinnerStandard)
        val spinnerPromo = findViewById<Spinner>(R.id.spinnerPromo)
        val spinnerArena = findViewById<Spinner>(R.id.spinnerArena)
        val spinnerAviso = findViewById<Spinner>(R.id.spinnerAviso)

        val listaSonidos = obtenerSonidosDelSistema()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listaSonidos)

        spinnerStandard.adapter = adapter
        spinnerPromo.adapter = adapter
        spinnerArena.adapter = adapter
        spinnerAviso.adapter = adapter

        configurarSpinner(spinnerStandard, listaSonidos, "sound_standard")
        configurarSpinner(spinnerPromo, listaSonidos, "sound_promo")
        configurarSpinner(spinnerArena, listaSonidos, "sound_arena")
        configurarSpinner(spinnerAviso, listaSonidos, "sound_aviso")
    }

    private fun configurarSpinner(spinner: Spinner, listaSonidos: List<SoundOption>, prefKey: String) {
        var esCargaInicial = true

        val savedUri = sharedPrefs.getString(prefKey, "")
        val savedIndex = listaSonidos.indexOfFirst { it.uri == savedUri }
        if (savedIndex >= 0) spinner.setSelection(savedIndex)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedSound = listaSonidos[position]
                sharedPrefs.edit().putString(prefKey, selectedSound.uri).apply()

                if (esCargaInicial) esCargaInicial = false
                else reproducirVistaPrevia(selectedSound.uri)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun reproducirVistaPrevia(uriString: String) {
        try {
            mediaPlayerPreview?.release()
            mediaPlayerPreview = MediaPlayer().apply {
                setDataSource(applicationContext, Uri.parse(uriString))
                prepare()
                start()
            }
            Handler(Looper.getMainLooper()).postDelayed({
                if (mediaPlayerPreview?.isPlaying == true) mediaPlayerPreview?.stop()
            }, 2500)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun obtenerSonidosDelSistema(): List<SoundOption> {
        val sounds = mutableListOf<SoundOption>()
        val ringtoneManager = RingtoneManager(this)
        ringtoneManager.setType(RingtoneManager.TYPE_ALL)

        val cursor = ringtoneManager.cursor
        while (cursor.moveToNext()) {
            val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
            val uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX) + "/" + cursor.getString(RingtoneManager.ID_COLUMN_INDEX)
            sounds.add(SoundOption(title, uri))
        }
        return sounds
    }

    private fun isNotificationServiceEnabled(): Boolean {
        return NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayerPreview?.release()
    }
}