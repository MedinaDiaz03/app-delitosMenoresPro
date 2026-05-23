package com.example.proyectofinal

import android.app.Application
import com.cloudinary.android.MediaManager
import com.google.firebase.FirebaseApp

class ProyectoFinalApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        // CONFIGURACIÓN DE CLOUDINARY
        val config = mapOf(
            "cloud_name" to "reportes_preset",
            "secure" to true
        )

        try {
            MediaManager.init(this, config)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}