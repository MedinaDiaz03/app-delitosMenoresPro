package com.example.proyectofinal.servicios

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.proyectofinal.MainActivity
import com.example.proyectofinal.R


object NotificationHelper {
    private const val CHANNEL_ID = "location_service_channel"
    private const val CHANNEL_NAME = "Servicio de ubicación"
    private const val NOTIFICATION_ID = 1

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)

            // 1. Canal para el Servicio de Ubicación (Prioridad Baja)
            val locationChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Canal para el servicio de monitoreo de ubicación"
                setShowBadge(false)
            }

            // 2. Canal para Alertas de Seguridad (Prioridad Alta)
            val alertChannel = NotificationChannel(
                "alertas_inmediatas",
                "Alertas de Seguridad",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de incidentes en tiempo real"
                enableVibration(true)
            }

            manager.createNotificationChannel(locationChannel)
            manager.createNotificationChannel(alertChannel)
        }
    }

    fun getForegroundNotification(context: Context, mensaje: String = "Monitoreando ubicación..."): NotificationCompat.Builder {
        // ... (sin cambios)
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("App Delitos Menores")
            .setContentText(mensaje)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
    }

    fun mostrarNotificacion(context: Context, titulo: String, mensaje: String, reporteId: String? = null) {
        val alertChannelId = "alertas_inmediatas"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear el Intent para abrir la App (Corregido para evitar reinicio a Home)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("reporteId", reporteId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(), // RequestCode único para no sobrescribir intents
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, alertChannelId)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
