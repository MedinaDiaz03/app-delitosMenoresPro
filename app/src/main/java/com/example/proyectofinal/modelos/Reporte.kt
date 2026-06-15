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
    val estado: String = "en_revision",
    val direccion: String? = null,
    // Campos de votación ciudadana
    val validacionesCount: Int = 0,   // Mantener para compatibilidad con datos existentes
    val rechazosCount: Int = 0,        // Mantener para compatibilidad con datos existentes
    val totalVotosCiudadanos: Int = 0,
    val votosReales: Int = 0,
    val votosFalsos: Int = 0,
    // Campos de votación policial
    val policiaHaVotado: Boolean = false,
    val estadoFinalPorPolicia: String? = null
)
