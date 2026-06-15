package com.example.proyectofinal

import android.app.Application
import com.cloudinary.android.MediaManager
import com.google.firebase.FirebaseApp

class ProyectoFinalApp : Application() {
    //Lo primero que inicia con la app
    override fun onCreate() {
        super.onCreate()
        // Crear canales de notificación al arrancar
        com.example.proyectofinal.servicios.NotificationHelper.createNotificationChannel(this)

        //La app inicia el servicio de firebase para tenerlo listo para usarlo
        FirebaseApp.initializeApp(this)

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