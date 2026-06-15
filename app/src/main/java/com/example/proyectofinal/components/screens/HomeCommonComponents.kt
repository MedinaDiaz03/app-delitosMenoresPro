package com.example.proyectofinal.components.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.ui.draw.scale
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
import com.example.proyectofinal.util.DistanceUtils
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

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
fun ControlesMapaCard(
    mostrarIconos: Boolean,
    onMostrarIconosChange: (Boolean) -> Unit,
    soloHoy: Boolean,
    onSoloHoyChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandido by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.width(if (expandido) 180.dp else 44.dp),
        shape = RoundedCornerShape(if (expandido) 16.dp else 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
        onClick = { if (!expandido) expandido = true }
    ) {
        Column(modifier = Modifier.padding(if (expandido) 12.dp else 10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (expandido) Arrangement.Start else Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    if (expandido) Icons.Default.FilterList else Icons.Default.Tune,
                    null,
                    tint = Color(0xFF1E3A8A),
                    modifier = Modifier.size(if (expandido) 16.dp else 24.dp)
                )
                if (expandido) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Filtros",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF1E3A8A),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { expandido = false },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, null, tint = Color(0xFF64748B))
                    }
                }
            }

            if (expandido) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Iconos", fontSize = 11.sp, color = Color(0xFF334155))
                    Switch(
                        checked = mostrarIconos,
                        onCheckedChange = onMostrarIconosChange,
                        modifier = Modifier.scale(0.6f),
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF1E3A8A))
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Solo hoy", fontSize = 11.sp, color = Color(0xFF334155))
                    Switch(
                        checked = soloHoy,
                        onCheckedChange = onSoloHoyChange,
                        modifier = Modifier.scale(0.6f),
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF1E3A8A))
                    )
                }
            }
        }
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
    userLocation: LatLng? = null,
    usuarioIdActual: String? = null,
    soloHoy: Boolean = false
) {
    val context = LocalContext.current
    val liveRepo = remember { LocationShareRepositorio() }
    var ubicacionesEnVivo by remember { mutableStateOf(listOf<LocationShare>()) }
    var reporteSeleccionado by remember { mutableStateOf<Reporte?>(null) }

    LaunchedEffect(Unit) {
        liveRepo.escuchar { ubicacionesEnVivo = it }
    }

    // Filtrar ubicaciones en vivo: solo mostrar de otros usuarios a menos de 100 metros
    val ubicacionesFiltradas = remember(userLocation, ubicacionesEnVivo, usuarioIdActual) {
        if (userLocation == null || usuarioIdActual == null) return@remember emptyList()
        ubicacionesEnVivo.filter { live ->
            live.usuarioId != usuarioIdActual && DistanceUtils.calcularDistanciaMetros(
                userLocation.latitude, userLocation.longitude,
                live.latitud, live.longitud
            ) < 100.0
        }
    }

    val reportesFiltrados = remember(reportes, soloHoy) {
        if (soloHoy) {
            val hoy = Calendar.getInstance()
            reportes.filter { reporte ->
                reporte.fecha?.let { timestamp ->
                    val fechaRepo = Calendar.getInstance().apply { time = timestamp.toDate() }
                    fechaRepo.get(Calendar.YEAR) == hoy.get(Calendar.YEAR) &&
                            fechaRepo.get(Calendar.DAY_OF_YEAR) == hoy.get(Calendar.DAY_OF_YEAR)
                } ?: false
            }
        } else {
            reportes
        }
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
        if (marcadoresVisibles) {
            RenderMarkers(reportesFiltrados) { reporteSeleccionado = it }
        }

        ubicacionesFiltradas.forEach { live ->
            key(live.usuarioId) {
                val markerState = rememberMarkerState(position = LatLng(live.latitud, live.longitud))
                
                // Actualizar posición si cambia en Firebase sin recrear el marcador
                LaunchedEffect(live.latitud, live.longitud) {
                    markerState.position = LatLng(live.latitud, live.longitud)
                }

                Marker(
                    state = markerState,
                    title = "Persona en estado SOS",
                    snippet = "Ubicación activa por emergencia",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                    alpha = 0.9f
                )
            }
        }
    }

    // Dialog al hacer clic en un marcador
    reporteSeleccionado?.let { reporte ->
        val distanciaText = if (userLocation != null) {
            val metros = DistanceUtils.calcularDistanciaMetros(
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
        key(reporte.id) {
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
fun BotonSOS(
    modifier: Modifier = Modifier,
    onSosActivado: suspend () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var mostrandoDialogo by remember { mutableStateOf(false) }

    // Diálogo de confirmación de llamada (opcional, pero lo dejamos por si el usuario quiere)
    if (mostrandoDialogo) {
        AlertDialog(
            onDismissRequest = { mostrandoDialogo = false },
            icon = { Icon(Icons.Default.LocalPolice, null, tint = Color(0xFFE04F5F), modifier = Modifier.size(36.dp)) },
            title = { Text("¿Llamar a emergencias?", fontWeight = FontWeight.Bold) },
            text = { Text("Se marcará el número 105 (Policía Nacional del Perú). ¿Confirmas la llamada?") },
            confirmButton = {
                Button(
                    onClick = {
                        mostrandoDialogo = false
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:105"))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE04F5F))
                ) {
                    Text("Llamar ahora", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrandoDialogo = false }) { Text("Cancelar") }
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
                        // Toque corto: muestra el diálogo de llamada (opcional, pero lo mantenemos)
                        mostrandoDialogo = true
                    },
                    onPress = {
                        // Mantener presionado 3 segundos -> activar SOS + llamar automáticamente
                        val job = scope.launch {
                            delay(3000)
                            // 1. Activar SOS (reporte + ubicación visible)
                            onSosActivado()
                            // 2. Llamar al 105 (sin diálogo adicional, acción directa)
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:105"))
                            context.startActivity(intent)
                        }
                        try { awaitRelease() } finally { job.cancel() }
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