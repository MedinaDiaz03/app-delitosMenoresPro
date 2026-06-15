package com.example.proyectofinal.servicios

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.proyectofinal.servicios.NotificationHelper

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // En un caso real, el servidor envía la notificación.
        // Aquí procesamos el mensaje cuando la app está en primer plano.
        val titulo = remoteMessage.notification?.title ?: "Alerta de Seguridad"
        val mensaje = remoteMessage.notification?.body ?: "Incidente reportado cerca de tu ubicación"
        val reporteId = remoteMessage.data["reporteId"]

        NotificationHelper.mostrarNotificacion(this, titulo, mensaje, reporteId)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Aquí se podría enviar el token al servidor si fuera necesario
    }
}
