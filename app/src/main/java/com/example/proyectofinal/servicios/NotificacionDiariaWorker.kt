package com.example.proyectofinal.servicios

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.proyectofinal.repositorios.AutenticacionRepositorio
import com.example.proyectofinal.repositorios.ReporteRepositorio

class NotificacionDiariaWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val authRepo = AutenticacionRepositorio()
        val reporteRepo = ReporteRepositorio()

        val usuario = authRepo.obtenerDatosUsuarioActual()

        // Solo ciudadanos reciben esta notificación
        if (usuario?.rol != "ciudadano") {
            return Result.success()
        }

        // Obtener reportes registrados
        val reportes = reporteRepo.obtenerReportes()
        val cantidad = reportes.size

        if (cantidad > 0) {
            mostrarNotificacion(applicationContext, cantidad)
        }

        return Result.success()
    }

    private fun mostrarNotificacion(context: Context, cantidad: Int) {
        val channelId = "incidentes_channel"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Incidentes diarios",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Resumen diario de incidentes reportados"
            }
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Resumen de seguridad")
            .setContentText("Se han registrado $cantidad incidentes recientemente en la plataforma.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // Verificar permiso antes de notificar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat.from(context).notify(1, notification)
            }
        } else {
            NotificationManagerCompat.from(context).notify(1, notification)
        }
    }
}
