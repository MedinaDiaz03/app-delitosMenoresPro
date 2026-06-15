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
        
        if (usuario == null) {
            // Si no hay sesión, al login
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        } else if (usuario?.rol.isNullOrBlank()) {
            // Si hay sesión pero NO tiene rol, a elegir uno
            navController.navigate("seleccion_rol") {
                popUpTo("home") { inclusive = true }
            }
        } else {
            // 1. Programar Notificación Diaria (Solo si ya está todo ok)
            val workRequest = PeriodicWorkRequestBuilder<NotificacionDiariaWorker>(
                1, TimeUnit.DAYS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "notificacion_diaria",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )

            // 2. Pedir Permisos necesarios
            val permisos = mutableListOf<String>()
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permisos.add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
            
            permisos.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
            permisos.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)

            if (permisos.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    permisos.toTypedArray(),
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
            "ciudadano" -> HomeCiudadanoScreen(navController)
            else -> {
                // En caso de que el rol sea nulo o vacío durante la carga inicial
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
