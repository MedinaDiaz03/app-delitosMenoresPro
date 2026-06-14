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
    val fotoUrl: String? = null,
    var anonimo: Boolean = false,
    val estado: String = "activo",
    val direccion: String? = null,
    val validacionesCount: Int = 0,
    val rechazosCount: Int = 0
)
