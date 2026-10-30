package com.bastian.macrobast.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.app.Notification
import android.util.Log
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.bastian.macrobast.utils.AlertManager
import com.bastian.macrobast.utils.CodeDetector

class TwitterListenerService : NotificationListenerService() {

    private lateinit var alertManager: AlertManager
    private var ultimoTweetProcesado: String = ""

    override fun onCreate() {
        super.onCreate()
        alertManager = AlertManager(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Para pruebas recuerda cambiar a "com.whatsapp"
        if (sbn.packageName == "com.twitter.android") {
            val extras = sbn.notification.extras
            val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
            val text = extras.getString(Notification.EXTRA_TEXT) ?: ""

            // Para pruebas recuerda cambiar a "Nombre de usuario"
            if (title.contains("Nikita", ignoreCase = true) || title.contains("nikgeneburn", ignoreCase = true)) {

                if (text == ultimoTweetProcesado) return
                ultimoTweetProcesado = text

                val resultado = CodeDetector.analizarTweet(text)

                when (resultado.tipo) {
                    CodeDetector.TipoCodigo.STANDARD_TARKOV -> {
                        Log.d("AlertaX", "¡STANDARD DETECTADO!: ${resultado.codigo}")
                        copiarAlPortapapeles(resultado.codigo)
                        alertManager.reproducirAlarmaStandard()
                        // URL para las ediciones Unheard, Edge of Darkness, Prepare for Escape, etc.
                        abrirPaginaTarkov("https://www.escapefromtarkov.com/promo/activate")
                    }
                    CodeDetector.TipoCodigo.PROMO_TARKOV -> {
                        Log.d("AlertaX", "¡PROMO EFT DETECTADO!: ${resultado.codigo}")
                        copiarAlPortapapeles(resultado.codigo)
                        alertManager.reproducirAlarmaPromo()
                        // URL principal de Tarkov
                        abrirPaginaTarkov("https://www.escapefromtarkov.com/promo/activate")
                    }
                    CodeDetector.TipoCodigo.PROMO_ARENA -> {
                        Log.d("AlertaX", "¡PROMO ARENA DETECTADO!: ${resultado.codigo}")
                        copiarAlPortapapeles(resultado.codigo)
                        alertManager.reproducirAlarmaArena()
                        // Redirige automáticamente al subdominio de Arena
                        abrirPaginaTarkov("https://arena.tarkov.com/promo/activate")
                    }
                    CodeDetector.TipoCodigo.NINGUNO -> {
                        if (CodeDetector.tienePalabraClave(text)) {
                            Log.d("AlertaX", "Aviso sin código: $text")
                            alertManager.reproducirAviso()
                        }
                    }
                }
            }
        }
    }

    private fun copiarAlPortapapeles(texto: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Código Tarkov", texto)
        clipboard.setPrimaryClip(clip)
        Log.d("AlertaX", "Copiado: $texto")
    }

    private fun abrirPaginaTarkov(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("AlertaX", "Error al abrir Chrome: ${e.message}")
        }
    }
}