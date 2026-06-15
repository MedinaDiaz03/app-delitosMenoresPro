package com.example.proyectofinal.servicios

import android.content.Context
import com.example.proyectofinal.repositorios.AutenticacionRepositorio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object FCMHelper {

    /**
     * Simula el envío de una notificación global.
     * En un entorno real, esto se haría a través de un backend que use Firebase Admin SDK.
     * Para efectos de este proyecto, simulamos la recepción local para el usuario si es ciudadano.
     */
    fun enviarNotificacionGlobal(context: Context, titulo: String, mensaje: String, reporteId: String? = null) {
        val authRepo = AutenticacionRepositorio()
        
        CoroutineScope(Dispatchers.Main).launch {
            val usuario = authRepo.obtenerDatosUsuarioActual()
            // Filtramos por rol: Las alertas de incidentes cercanos son para ciudadanos.
            // Aunque en la realidad el autor no debería recibir su propia notificación,
            // aquí simulamos el sistema de alertas para los ciudadanos.
            if (usuario?.rol == "ciudadano") {
                NotificationHelper.mostrarNotificacion(context, titulo, mensaje, reporteId)
            }
        }
    }
}
