package com.example.proyectofinal.modelos

import com.google.firebase.Timestamp

data class Reporte(
    val id: String = "",
    val usuarioId: String = "",
    val usuarioNombre: String = "",
    val categoria: String = "",
    val descripcion: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val fecha: Timestamp = Timestamp.now(),
    val fotoUrl: String? = null
)
