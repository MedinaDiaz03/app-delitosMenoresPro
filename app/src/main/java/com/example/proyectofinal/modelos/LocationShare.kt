package com.example.proyectofinal.modelos

data class LocationShare(
    val usuarioId: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val activo: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
