package com.example.proyectofinal.modelos

import com.google.firebase.Timestamp

data class Reporte(
    val id: String = "",
    val usuarioId: String = "",
    val usuarioNombre: String = "",
    val categoria: String = "",
    val descripcion: String = "",//la descripción puede subirse vacio
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val fecha: Timestamp = Timestamp.now(),//jala la hora exacta desde firebase
    val fotoUrl: String? = null,//la foto en un reporte es opcional
    var anonimo: Boolean = false
)
