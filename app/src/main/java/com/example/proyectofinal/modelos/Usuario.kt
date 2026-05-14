package com.example.proyectofinal.modelos

data class Usuario(
    var id: String = "",
    var nombres: String = "",
    var apellidos: String = "",
    var dni: String = "",
    var correo: String = "",
    var nivelConfianza: Int = 0,
    var reportesValidados: Int = 0
)
