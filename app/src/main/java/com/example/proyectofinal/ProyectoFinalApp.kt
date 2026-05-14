package com.example.proyectofinal

import android.app.Application
import com.google.firebase.FirebaseApp

class ProyectoFinalApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
