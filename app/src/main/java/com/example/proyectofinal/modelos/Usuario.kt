package com.example.proyectofinal.modelos

data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val rol: String = "ciudadano",
    val verificado: Boolean = false,
    val nivelConfianza: Int = 0,
    val telefonoEmergencia: String = ""
)