package com.example.proyectofinal

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.cloudinary.android.MediaManager
import com.example.proyectofinal.servicios.ExpirarReportesWorker
import com.google.firebase.FirebaseApp
import java.util.concurrent.TimeUnit

class ProyectoFinalApp : Application() {
    //Lo primero que inicia con la app
    override fun onCreate() {
        super.onCreate()
        // Crear canales de notificación al arrancar
        com.example.proyectofinal.servicios.NotificationHelper.createNotificationChannel(this)

        //La app inicia el servicio de firebase para tenerlo listo para usarlo
        FirebaseApp.initializeApp(this)

        // Programar limpieza de reportes antiguos (Fase 5 - 1 año de persistencia)
        val expirarWorkRequest = PeriodicWorkRequestBuilder<ExpirarReportesWorker>(
            24, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "expirar_reportes",
            ExistingPeriodicWorkPolicy.KEEP,
            expirarWorkRequest
        )

        // CONFIGURACIÓN DE CLOUDINARY
        val config = mapOf(
            "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
            "api_key" to BuildConfig.CLOUDINARY_API_KEY,
            "api_secret" to BuildConfig.CLOUDINARY_API_SECRET,
            "secure" to true
        )

        //En caso la conexión con cloudinary falla
        //Esto le dice a la app que no se cierre
        //Solo que suelte un mensaje de error y siga arrancando la app
        try {
            MediaManager.init(this, config)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}