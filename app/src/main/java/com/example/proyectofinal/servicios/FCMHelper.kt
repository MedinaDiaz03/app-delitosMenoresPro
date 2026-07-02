package com.example.proyectofinal.servicios

import android.content.Context
import com.example.proyectofinal.repositorios.AutenticacionRepositorio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object FCMHelper {

    fun enviarNotificacionGlobal(
        context: Context,
        titulo: String,
        mensaje: String,
        reporteId: String? = null,
        autorId: String = ""
    ) {
        val authRepo = AutenticacionRepositorio()

        CoroutineScope(Dispatchers.Main).launch {
            val usuario = authRepo.obtenerDatosUsuarioActual()

            if (usuario?.rol == "ciudadano" && usuario.uid != autorId) {
                NotificationHelper.mostrarNotificacion(context, titulo, mensaje, reporteId)
            }
        }
    }
}