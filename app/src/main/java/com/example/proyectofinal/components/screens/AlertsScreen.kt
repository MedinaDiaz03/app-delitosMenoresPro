package com.example.proyectofinal.components.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyectofinal.components.navigation.BottomNavigationBar
import com.example.proyectofinal.repositorios.AutenticacionRepositorio
import com.example.proyectofinal.ui.theme.GreenPrimary
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AlertsScreen(navController: NavController) {
    val authRepo = remember { AutenticacionRepositorio() }
    var esPolicia by remember { mutableStateOf(false) }
    var usuarioNombre by remember { mutableStateOf("") }
    
    // Gestión de permisos de ubicación
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    LaunchedEffect(Unit) {
        val usuario = authRepo.obtenerDatosUsuarioActual()
        esPolicia = usuario?.rol == "policia"
        usuarioNombre = usuario?.nombre ?: ""
        
        // Solicitar permiso al entrar si no está concedido
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = GreenPrimary)
                    }
                },
                title = { 
                    Text(
                        if (esPolicia) "Centro de Alertas (Oficial)" else "Alertas de Seguridad", 
                        color = GreenPrimary, 
                        fontWeight = FontWeight.Bold 
                    ) 
                }
            )
        },
        bottomBar = { BottomNavigationBar(navController, esPolicia = esPolicia) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icono informativo
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = GreenPrimary.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                val titulo = if (esPolicia) {
                    "Estado: Escaneando zona de patrullaje"
                } else {
                    "Buscando alertas en tu zona"
                }

                val descripcion = if (!locationPermissionState.status.isGranted) {
                    "Se requiere acceso a su ubicación para mostrar las alertas cercanas. Por favor, conceda los permisos necesarios."
                } else if (esPolicia) {
                    "Oficial $usuarioNombre, el sistema está monitoreando reportes críticos en tiempo real. " +
                    "Para recibir alertas instantáneas, asegúrese de mantener su GPS activo."
                } else {
                    "No hay alertas críticas en este momento. Te notificaremos si se reporta un incidente cerca de tu ubicación actual."
                }

                Text(
                    text = if (!locationPermissionState.status.isGranted) "Permiso de GPS requerido" else titulo,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (!locationPermissionState.status.isGranted) Color.Red else GreenPrimary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = descripcion,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                if (!locationPermissionState.status.isGranted) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { locationPermissionState.launchPermissionRequest() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Icon(Icons.Default.Settings, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Habilitar Permisos")
                    }
                }

                if (esPolicia && locationPermissionState.status.isGranted) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { navController.navigate("home") },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    ) {
                        Text("Ver Mapa de Incidentes")
                    }
                }
            }
        }
    }
}
