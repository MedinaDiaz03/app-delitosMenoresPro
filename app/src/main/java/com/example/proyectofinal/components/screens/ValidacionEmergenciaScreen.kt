package com.example.proyectofinal.components.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyectofinal.helpers.RolHelper
import com.example.proyectofinal.ui.theme.GreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValidacionEmergenciaScreen(
    navController: NavController,
    rolHelper: RolHelper,
    reporteId: String,
    onValidarComoPolicia: (String) -> Unit = {},
    onSolicitarValidacion: (String) -> Unit = {}
) {
    var esPolicia by remember { mutableStateOf(false) }
    val colores = MaterialTheme.colorScheme

    LaunchedEffect(Unit) {
        rolHelper.obtenerRol { rol ->
            esPolicia = rol == "policia"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verificar Emergencia", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = GreenPrimary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (esPolicia) Icons.Default.GppGood else Icons.Default.Group,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = GreenPrimary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = if (esPolicia) "Confirmación Policial" else "Validación de la Comunidad",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "ID del Reporte: $reporteId",
                fontSize = 14.sp,
                color = colores.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = if (esPolicia) 
                    "Como oficial, tu validación eleva la prioridad de este reporte inmediatamente." 
                    else "Al validar como testigo, ayudas a los servicios de emergencia a priorizar reportes reales.",
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                color = colores.onSurface
            )
            
            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { 
                    if (esPolicia) onValidarComoPolicia(reporteId) 
                    else onSolicitarValidacion(reporteId)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    if (esPolicia) "CONFIRMAR EMERGENCIA ✅" else "SOY TESTIGO / APOYAR REPORTE 👍",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
