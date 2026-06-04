package com.example.proyectofinal.helpers

import kotlinx.coroutines.flow.Flow

interface RolHelper {
    fun obtenerRolActual(): Flow<String>
}