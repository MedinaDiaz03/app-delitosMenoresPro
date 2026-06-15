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
        // ... (sin cambios)
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                alertChannelId,
                "Alertas de Seguridad",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de incidentes en tiempo real"
            }
            manager.createNotificationChannel(channel)
        }

        // Crear el Intent para abrir la App
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("reporteId", reporteId) // Pasamos el ID del reporte
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, alertChannelId)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // Acción al hacer clic
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
