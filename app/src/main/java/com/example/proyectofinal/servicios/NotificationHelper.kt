package com.example.proyectofinal.servicios

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.proyectofinal.R


object NotificationHelper {
    private const val CHANNEL_ID = "location_service_channel"
    private const val CHANNEL_NAME = "Servicio de ubicación"
    private const val NOTIFICATION_ID = 1

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Canal para el servicio de monitoreo de ubicación"
                setShowBadge(false)
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun getForegroundNotification(context: Context, mensaje: String = "Monitoreando ubicación..."): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("App Delitos Menores")
            .setContentText(mensaje)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation) // ícono del sistema (o crea uno propio)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
    }
}