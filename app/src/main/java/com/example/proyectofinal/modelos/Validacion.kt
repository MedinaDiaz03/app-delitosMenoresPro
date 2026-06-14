package com.example.proyectofinal.modelos

import com.google.firebase.Timestamp

data class Validacion(
    val id: String = "",
    val reporteId: String = "",
    val usuarioId: String = "",
    val esReal: Boolean = true,
    val fecha: Timestamp = Timestamp.now()
)
