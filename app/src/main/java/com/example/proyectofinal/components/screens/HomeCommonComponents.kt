package com.example.proyectofinal.components.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyectofinal.modelos.LocationShare
import com.example.proyectofinal.modelos.Reporte
import com.example.proyectofinal.repositorios.AutenticacionRepositorio
import com.example.proyectofinal.repositorios.LocationRepositorio
import com.example.proyectofinal.repositorios.LocationShareRepositorio
import com.example.proyectofinal.repositorios.ReporteRepositorio
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import android.widget.Toast
import com.example.proyectofinal.servicios.FCMHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun obtenerColorMarker(categoria: String): Float {
    return when (categoria.lowercase()) {
        "sos" -> BitmapDescriptorFactory.HUE_RED
        "robo" -> BitmapDescriptorFactory.HUE_RED
        "vandalismo" -> BitmapDescriptorFactory.HUE_ORANGE
        "pelea" -> BitmapDescriptorFactory.HUE_YELLOW
        "drogas" -> BitmapDescriptorFactory.HUE_VIOLET
        "acoso" -> BitmapDescriptorFactory.HUE_ROSE
        "infraestructura" -> BitmapDescriptorFactory.HUE_AZURE
        else -> BitmapDescriptorFactory.HUE_RED
    }
}

@Composable
fun MapaConReportes(
    reportes: List<Reporte>,
    camaraState: CameraPositionState,
    locationGranted: Boolean,
    mapaOscuro: Boolean,
    esPolicia: Boolean,
    marcadoresVisibles: Boolean,
    navController: NavController,
    userLocation: LatLng? = null
) {
    val context = LocalContext.current
    val liveRepo = remember { LocationShareRepositorio() }
    var ubicacionesEnVivo by remember { mutableStateOf(listOf<LocationShare>()) }
    var reporteSeleccionado by remember { mutableStateOf<Reporte?>(null) }

    LaunchedEffect(Unit) {
        liveRepo.escuchar { ubicacionesEnVivo = it }
    }

    val propiedadesMapa = MapProperties(
        isMyLocationEnabled = locationGranted,
        mapStyleOptions = if (mapaOscuro) {
            MapStyleOptions.loadRawResourceStyle(context, com.example.proyectofinal.R.raw.map_dark_style)
        } else null
    )

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = camaraState,
        properties = propiedadesMapa,
        uiSettings = MapUiSettings(myLocationButtonEnabled = false)
    ) {
        if (!esPolicia || marcadoresVisibles) {
            RenderMarkers(reportes) { reporteSeleccionado = it }
        }

        ubicacionesEnVivo.forEach { live ->
            Marker(
                state = MarkerState(position = LatLng(live.latitud, live.longitud)),
                title = "Usuario en movimiento",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                alpha = 0.8f
            )
        }
    }

    // Dialog al hacer clic en un marcador
    reporteSeleccionado?.let { reporte ->
        val distanciaText = if (userLocation != null) {
            val metros = calcularDistancia(
                userLocation.latitude, userLocation.longitude,
                reporte.latitud, reporte.longitud
            ).toInt()
            if (metros < 1000) "${metros}m" else "${"%.1f".format(metros / 1000.0)}km"
        } else null

        AlertDialog(
            onDismissRequest = { reporteSeleccionado = null },
            icon = {
                Icon(
                    Icons.Default.LocationOn,
                    null,
                    tint = Color(0xFFE04F5F),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(reporte.categoria.uppercase(), fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (distanciaText != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.NearMe, null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                            Text("Distancia: $distanciaText", fontSize = 14.sp, color = Color(0xFF1E3A8A))
                        }
                    }
                    if (reporte.descripcion.isNotEmpty()) {
                        Text(reporte.descripcion, fontSize = 13.sp, color = Color(0xFF334155))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        reporteSeleccionado = null
                        navController.navigate("report_detail/${reporte.id}")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) { Text("Ver detalles") }
            },
            dismissButton = {
                TextButton(onClick = { reporteSeleccionado = null }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }
}

@Composable
fun RenderMarkers(reportes: List<Reporte>, onReporteClick: (Reporte) -> Unit) {
    reportes.forEach { reporte ->
        if (reporte.latitud != 0.0 && reporte.longitud != 0.0) {
            val color = obtenerColorMarker(reporte.categoria)
            Marker(
                state = MarkerState(position = LatLng(reporte.latitud, reporte.longitud)),
                title = reporte.categoria.uppercase(),
                snippet = reporte.descripcion.take(60),
                icon = BitmapDescriptorFactory.defaultMarker(color),
                onClick = {
                    onReporteClick(reporte)
                    true
                }
            )
        }
    }
}

@Composable
fun SecurityStatusCard() {
    val colores = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(0.65f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Mapa de Seguridad", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colores.onSurface)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colores.primary))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Estado: Seguro", fontSize = 14.sp, color = Color(0xFF1E3A8A))
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { 0.7f },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                color = colores.primary,
                trackColor = colores.outlineVariant
            )
        }
    }
}

@Composable
fun FilterChipsRow() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChipTemplate("Todo", Icons.Default.FilterList, true)
        FilterChipTemplate("Hospitales", Icons.Default.LocalHospital, false)
        FilterChipTemplate("Alertas", Icons.Default.Report, false)
    }
}

@Composable
fun FilterChipTemplate(text: String, icon: ImageVector, isSelected: Boolean) {
    val colores = MaterialTheme.colorScheme
    Surface(
        color = if (isSelected) colores.primary else colores.surface,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp,
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, colores.outlineVariant) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) Color.White else Color(0xFF1E3A8A)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                color = if (isSelected) Color.White else Color(0xFF1E3A8A)
            )
        }
    }
}

@Composable
fun MapActionButton(icon: ImageVector, onClick: () -> Unit = {}) {
    val colores = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.size(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = colores.surface,
        shadowElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, colores.outlineVariant),
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = colores.primary)
        }
    }
}

@Composable
fun BotonSOS(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authRepo = remember { AutenticacionRepositorio() }
    val reportRepo = remember { ReporteRepositorio() }
    val locationRepo = remember { LocationRepositorio(context) }

    var mostrarDialogo by remember { mutableStateOf(false) }
    var confirmacion by remember { mutableStateOf(false) }

    // Diálogos de llamada (se activan con un clic normal)
    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            icon = { Icon(Icons.Default.LocalPolice, null, tint = Color(0xFFE04F5F), modifier = Modifier.size(36.dp)) },
            title = { Text("¿Llamar a emergencias?", fontWeight = FontWeight.Bold) },
            text  = { Text("Se marcará el número 105 (Policía Nacional del Perú). ¿Confirmas la llamada?") },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogo = false
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:105"))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE04F5F))
                ) {
                    Text("Llamar ahora", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogo = false
                    confirmacion = true
                }) {
                    Text("Cancelar")
                }
            }
        )
    } else if (confirmacion) {
        AlertDialog(
            onDismissRequest = { confirmacion = false },
            icon = { Icon(Icons.Default.LocalPolice, null, tint = Color(0xFFE04F5F), modifier = Modifier.size(36.dp)) },
            title = { Text("¿Estás seguro(a) que quieres cancelar la llamada?", fontWeight = FontWeight.Bold) },
            confirmButton = {
                Button(
                    onClick = {
                        confirmacion = false
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:105"))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE04F5F))
                ) {
                    Text("Llamar ahora", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmacion = false }) {
                    Text("Sí, estoy seguro")
                }
            }
        )
    }

    Surface(
        shape = CircleShape,
        modifier = modifier
            .size(72.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { 
                        // Toque rápido: Abre el flujo de llamada
                        mostrarDialogo = true 
                    },
                    onPress = {
                        // Iniciar cronómetro de 3 segundos al presionar
                        val job = scope.launch {
                            delay(3000) // Esperar 3 segundos exactos
                            
                            // Acción SOS Automática si sigue presionado
                            val usuario = authRepo.obtenerDatosUsuarioActual()
                            val ubicacion = locationRepo.obtenerUbicacionActual()

                            if (usuario != null && ubicacion != null) {
                                val nuevoSOS = Reporte(
                                    usuarioId = usuario.uid,
                                    usuarioNombre = usuario.nombre,
                                    categoria = "sos",
                                    descripcion = "ALERTA DE EMERGENCIA (ENVÍO AUTOMÁTICO 3s)",
                                    latitud = ubicacion.latitude,
                                    longitud = ubicacion.longitude,
                                    estado = "activo"
                                )
                                reportRepo.enviarReporte(nuevoSOS)
                                FCMHelper.enviarNotificacionGlobal(
                                    context, 
                                    "🚨 SOS: Emergencia Detectada", 
                                    "Un vecino ha activado un SOS cerca de tu ubicación."
                                )
                                Toast.makeText(context, "🚨 SOS ENVIADO AUTOMÁTICAMENTE", Toast.LENGTH_LONG).show()
                            }
                        }
                        try {
                            awaitRelease() // Esperar a que el usuario suelte
                        } finally {
                            job.cancel() // Si suelta antes de los 3s, se cancela el envío automático
                        }
                    }
                )
            },
        color = Color(0xFFE04F5F),
        shadowElevation = 8.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.LocalPolice, null, tint = Color.White, modifier = Modifier.size(22.dp))
            Text("SOS", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
        }
    }
}
