package com.example.proyectofinal.components.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyectofinal.components.navigation.BottomNavigationBar
import com.example.proyectofinal.modelos.Reporte
import com.example.proyectofinal.repositorios.AutenticacionRepositorio
import com.example.proyectofinal.repositorios.LocationRepositorio
import com.example.proyectofinal.repositorios.ReporteRepositorio
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.LatLng
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AlertsScreen(navController: NavController) {
    val context = LocalContext.current
    val authRepo = remember { AutenticacionRepositorio() }
    val reporteRepo = remember { ReporteRepositorio() }
    val locationRepo = remember { LocationRepositorio(context) }

    var esPolicia by remember { mutableStateOf(false) }
    var reportesCercanos by remember { mutableStateOf<List<Reporte>>(emptyList()) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var cargando by remember { mutableStateOf(true) }

    val locationPermission = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(locationPermission.status.isGranted) {
        if (!locationPermission.status.isGranted) {
            locationPermission.launchPermissionRequest()
            cargando = false
            return@LaunchedEffect
        }
        esPolicia = authRepo.obtenerDatosUsuarioActual()?.rol == "policia"

        val loc = locationRepo.obtenerUbicacionActual()
        if (loc != null) {
            val latLng = LatLng(loc.latitude, loc.longitude)
            userLocation = latLng
            reportesCercanos = reporteRepo.obtenerReportesEnRadio(latLng.latitude, latLng.longitude, 1000.0)
        }
        cargando = false
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Alertas de seguridad",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF1E3A8A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC))
            )
        },
        bottomBar = { BottomNavigationBar(navController, esPolicia = esPolicia) }
    ) { paddingValues ->

        if (!locationPermission.status.isGranted) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        modifier = Modifier.size(88.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.LocationOff, null, modifier = Modifier.size(44.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Text("Permiso de ubicación requerido", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E3A8A))
                    Text(
                        "Necesitamos acceso a tu ubicación para mostrarte alertas cercanas.",
                        color = Color(0xFF3B82F6), textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { locationPermission.launchPermissionRequest() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) { Text("Habilitar ubicación", fontWeight = FontWeight.Bold) }
                }
            }
            return@Scaffold
        }

        if (cargando) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(
                    "Incidentes cercanos",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Reportes activos en un radio de 1 km",
                    fontSize = 14.sp,
                    color = Color(0xFF3B82F6)
                )
            }

            if (reportesCercanos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF16A34A).copy(alpha = 0.08f),
                            modifier = Modifier.size(88.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(44.dp), tint = Color(0xFF16A34A))
                            }
                        }
                        Text("Sin alertas activas", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF1E3A8A))
                        Text("No hay reportes activos en 1 km de tu ubicación.", color = Color(0xFF3B82F6), textAlign = TextAlign.Center)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(reportesCercanos) { reporte ->
                        val distancia = userLocation?.let { loc ->
                            reporteRepo.calcularDistanciaMetros(
                                loc.latitude, loc.longitude,
                                reporte.latitud, reporte.longitud
                            ).toInt()
                        }
                        AlertaItem(
                            reporte = reporte,
                            distanciaMetros = distancia,
                            onClick = { navController.navigate("report_detail/${reporte.id}") }
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun AlertaItem(
    reporte: Reporte,
    distanciaMetros: Int?,
    onClick: () -> Unit
) {
    val colorCategoria = when (reporte.categoria.lowercase()) {
        "robo" -> Color(0xFFEF4444)
        "vandalismo" -> Color(0xFFF97316)
        "pelea" -> Color(0xFFEAB308)
        "drogas" -> Color(0xFFA855F7)
        "acoso" -> Color(0xFFEC4899)
        "infraestructura" -> Color(0xFF3B82F6)
        "sos" -> Color(0xFFEF4444)
        else -> Color(0xFF6B7280)
    }

    val fecha = try {
        reporte.fecha?.toDate()?.let {
            SimpleDateFormat("dd MMM • HH:mm", Locale.getDefault()).format(it)
        } ?: "—"
    } catch (_: Exception) { "—" }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colorCategoria.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (reporte.categoria.lowercase()) {
                        "robo" -> Icons.Default.GppBad
                        "vandalismo" -> Icons.Default.Edit
                        "pelea" -> Icons.Default.Groups
                        "drogas" -> Icons.Default.MedicalServices
                        "acoso" -> Icons.Default.RecordVoiceOver
                        "infraestructura" -> Icons.Default.Build
                        "sos" -> Icons.Default.Warning
                        else -> Icons.Default.Report
                    },
                    contentDescription = null,
                    tint = colorCategoria,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        reporte.categoria.uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = colorCategoria,
                        fontSize = 14.sp
                    )
                    if (distanciaMetros != null) {
                        Surface(
                            color = colorCategoria.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                if (distanciaMetros < 1000) "${distanciaMetros}m"
                                else "${"%.1f".format(distanciaMetros / 1000.0)}km",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorCategoria
                            )
                        }
                    }
                }
                Text(
                    reporte.descripcion.ifEmpty { "Sin descripción" },
                    fontSize = 13.sp,
                    color = Color(0xFF1E3A8A),
                    maxLines = 2
                )
                Text(
                    reporte.direccion ?: fecha,
                    fontSize = 11.sp,
                    color = Color(0xFF3B82F6)
                )
            }

            Icon(Icons.Default.ChevronRight, null, tint = Color(0xFFBFDBFE), modifier = Modifier.size(18.dp))
        }
    }
}
