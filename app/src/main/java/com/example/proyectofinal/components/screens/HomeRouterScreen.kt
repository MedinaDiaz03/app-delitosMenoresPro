package com.example.proyectofinal.components.screens

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.proyectofinal.modelos.Usuario
import com.example.proyectofinal.repositorios.AutenticacionRepositorio
import com.example.proyectofinal.servicios.NotificacionDiariaWorker
import java.util.concurrent.TimeUnit

@Composable
fun HomeRouterScreen(navController: NavController) {
    val context = LocalContext.current
    val authRepositorio = remember { AutenticacionRepositorio() }
    var usuario by remember { mutableStateOf<Usuario?>(null) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        usuario = authRepositorio.obtenerDatosUsuarioActual()
        
        if (usuario?.rol == "ciudadano") {
            // 1. Programar Notificación Diaria
            val workRequest = PeriodicWorkRequestBuilder<NotificacionDiariaWorker>(
                1, TimeUnit.DAYS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "notificacion_diaria",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )

            // 2. Pedir Permisos de Notificación (Solo Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }

        cargando = false
    }

    if (cargando) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        when (usuario?.rol) {
            "policia" -> HomePoliciaScreen(navController)
            else -> HomeCiudadanoScreen(navController)
        }
    }
}
