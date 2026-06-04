package com.example.proyectofinal.viewmodels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.proyectofinal.helpers.RolHelper

@Composable
fun ValidacionEmergenciaScreen(
    rolHelper: RolHelper,
    reporteId: String  // añade este parámetro
) {
    val rol by rolHelper.obtenerRolActual().collectAsState(initial = "comun")
    val esPolicia = rol == "policia"

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Validar emergencia - ID: $reporteId", style = MaterialTheme.typography.headlineSmall)

        if (esPolicia) {
            Button(onClick = {
                // Aquí llamarías a una función de validación real
                // Por ahora, solo un toast o log
            }) {
                Text("Validar esta emergencia (policía)")
            }
        } else {
            Button(onClick = {
                // Solicitar validación
            }) {
                Text("Soy testigo, solicitar validación")
            }
        }
    }
}