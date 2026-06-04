package com.example.proyectofinal.components.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.proyectofinal.helpers.RolHelper

@Composable
fun ValidacionEmergenciaScreen(
    rolHelper: RolHelper,
    reporteId: String,  // ← agregado
    onValidarComoPolicia: (String) -> Unit = {},   // lambda temporal
    onSolicitarValidacion: (String) -> Unit = {}   // lambda temporal
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
            Button(onClick = { onValidarComoPolicia(reporteId) }) {
                Text("Validar esta emergencia (policía)")
            }
        } else {
            Button(onClick = { onSolicitarValidacion(reporteId) }) {
                Text("Soy testigo, solicitar validación")
            }
        }
    }
}