package com.example.proyectofinal.components.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.proyectofinal.modelos.Reporte
import com.example.proyectofinal.modelos.Usuario
import com.example.proyectofinal.repositorios.AutenticacionRepositorio
import com.example.proyectofinal.repositorios.ReporteRepositorio
import com.example.proyectofinal.servicios.GeocodingService
import com.google.android.gms.maps.model.LatLng
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(navController: NavController, reporteId: String) {
    val context = LocalContext.current
    val authRepo = remember { AutenticacionRepositorio() }
    val repo = remember { ReporteRepositorio() }
    val scope = rememberCoroutineScope()
    var reporte by remember { mutableStateOf<Reporte?>(null) }
    var usuarioActual by remember { mutableStateOf<Usuario?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var direccionResuelta by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(reporteId) {
        usuarioActual = authRepo.obtenerDatosUsuarioActual()
        reporte = repo.obtenerReportePorId(reporteId)
        val r = reporte
        if (r != null && r.direccion.isNullOrEmpty() && (r.latitud != 0.0 || r.longitud != 0.0)) {
            direccionResuelta = GeocodingService.getAddressFromLatLng(
                context, LatLng(r.latitud, r.longitud)
            )
        }
        cargando = false
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Detalle del reporte",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E3A8A))
            )
        }
    ) { padding ->
        when {
            cargando -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            reporte == null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No se pudo cargar el reporte.", color = Color(0xFF475569))
            }

            else -> DetalleContenido(
                reporte = reporte!!,
                padding = padding,
                colores = MaterialTheme.colorScheme,
                esPolicia = usuarioActual?.rol == "policia",
                esPropio = usuarioActual?.uid == reporte!!.usuarioId,
                direccionResuelta = direccionResuelta,
                onCambiarEstado = { nuevoEstado ->
                    scope.launch {
                        repo.actualizarEstadoReporte(reporteId, nuevoEstado)
                        navController.popBackStack()
                    }
                },
                onValidarClick = {
                    navController.navigate("validacion/${reporteId}")
                }
            )
        }
    }
}

@Composable
fun DetalleContenido(
    reporte: Reporte,
    padding: PaddingValues,
    colores: ColorScheme,
    esPolicia: Boolean,
    esPropio: Boolean = false,
    direccionResuelta: String? = null,
    onCambiarEstado: (String) -> Unit,
    onValidarClick: () -> Unit
) {
    val fecha = try {
        if (reporte.fecha != null)
            SimpleDateFormat("dd MMM, yyyy • HH:mm", Locale.getDefault()).format(reporte.fecha.toDate())
        else "Fecha no disponible"
    } catch (_: Exception) { "Fecha no disponible" }

    val tieneImagen = !reporte.fotoUrl.isNullOrEmpty() && reporte.fotoUrl.startsWith("http")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ── FOTO DE EVIDENCIA ──
        if (tieneImagen) {
            Card(
                modifier = Modifier.fillMaxWidth().height(240.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(reporte.fotoUrl),
                    contentDescription = "Evidencia",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Sin evidencia fotográfica", color = Color(0xFF64748B), fontSize = 14.sp)
            }
        }

        // ── CATEGORÍA, ESTADO Y FECHA ──
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    reporte.categoria.uppercase(),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                EstadoBadge(estado = reporte.estado)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.CalendarMonth, null, tint = Color(0xFF64748B), modifier = Modifier.size(15.dp))
                Text(fecha, fontSize = 13.sp, color = Color(0xFF64748B))
            }
        }

        HorizontalDivider(color = Color(0xFFE2E8F0))

        // ── QUIÉN REPORTÓ ──
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(36.dp).background(Color(0xFFF1F5F9), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
            Column {
                Text("Reportado por", fontSize = 11.sp, color = Color(0xFF64748B))
                Text(
                    reporte.usuarioNombre.ifEmpty { "Vecino de la comunidad" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B)
                )
            }
        }

        // ── UBICACIÓN ──
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(36.dp).background(Color(0xFFF1F5F9), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocationOn, null, tint = Color(0xFFE04F5F), modifier = Modifier.size(18.dp))
            }
            Column {
                Text("Ubicación del suceso", fontSize = 11.sp, color = Color(0xFF64748B))
                val textoUbicacion = when {
                    !reporte.direccion.isNullOrEmpty() -> reporte.direccion!!
                    !direccionResuelta.isNullOrEmpty() -> direccionResuelta!!
                    reporte.latitud != 0.0 || reporte.longitud != 0.0 -> "Obteniendo dirección..."
                    else -> "Ubicación no disponible"
                }
                Text(
                    textoUbicacion,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1E293B)
                )
            }
        }

        HorizontalDivider(color = Color(0xFFE2E8F0))

        // ── DESCRIPCIÓN ──
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Detalle de los hechos",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF1E293B)
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = reporte.descripcion.ifEmpty { "El usuario no añadió descripción." },
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = Color(0xFF334155)
                )
            }
        }

        // ── PANEL DE VOTACIÓN ──
        if (reporte.totalVotosCiudadanos > 0 || reporte.policiaHaVotado) {
            HorizontalDivider(color = Color(0xFFE2E8F0))
            VotacionPanel(reporte = reporte)
        }

        // ── ACCIONES ──
        HorizontalDivider(color = Color(0xFFE2E8F0))

        if (esPolicia) {
            Text(
                "Gestión del reporte",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF1E293B)
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { onCambiarEstado("en_proceso") },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text("Marcar en proceso", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { onCambiarEstado("resuelto") },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text("Marcar como resuelto", color = Color.White, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { onCambiarEstado("falso") },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE04F5F)),
                    border = BorderStroke(1.dp, Color(0xFFE04F5F))
                ) {
                    Text("Marcar como falso", fontWeight = FontWeight.Bold)
                }
            }
        } else if (!esPropio) {
            Button(
                onClick = onValidarClick,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Calificar reporte", color = Color.White, fontWeight = FontWeight.Bold)
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF1F5F9),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Este es tu reporte — no puedes validarlo",
                    modifier = Modifier.padding(14.dp),
                    color = Color(0xFF3B82F6),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun EstadoBadge(estado: String) {
    val (bgColor, textColor, label) = when (estado) {
        "en_revision" -> Triple(Color(0xFFFFF3CD), Color(0xFF856404), "En revisión")
        "activo"      -> Triple(Color(0xFFFFF3CD), Color(0xFF856404), "En revisión")
        "verificado"  -> Triple(Color(0xFFD1FAE5), Color(0xFF166534), "Verificado")
        "falso"       -> Triple(Color(0xFFFEE2E2), Color(0xFF991B1B), "Falsa alarma")
        "resuelto"    -> Triple(Color(0xFFDBEAFE), Color(0xFF1E40AF), "Resuelto")
        "en_proceso"  -> Triple(Color(0xFFFFEDD5), Color(0xFF9A3412), "En proceso")
        else          -> Triple(Color(0xFFF1F5F9), Color(0xFF475569), estado)
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
private fun VotacionPanel(reporte: Reporte) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "Validaciones de la comunidad",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF1E293B)
        )

        // Veredicto policial (si existe)
        if (reporte.policiaHaVotado) {
            val esVerificadoPorPolicia = reporte.estadoFinalPorPolicia == "verificado"
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (esVerificadoPorPolicia) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
                ),
                border = BorderStroke(1.dp, if (esVerificadoPorPolicia) Color(0xFFBBF7D0) else Color(0xFFFECACA)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Shield,
                        null,
                        tint = if (esVerificadoPorPolicia) Color(0xFF166534) else Color(0xFF991B1B),
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            if (esVerificadoPorPolicia) "Verificado por autoridad" else "Falsa alarma según autoridad",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (esVerificadoPorPolicia) Color(0xFF166534) else Color(0xFF991B1B)
                        )
                        Text(
                            "Un oficial emitió veredicto definitivo",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        // Conteo ciudadano
        if (reporte.totalVotosCiudadanos > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "${reporte.totalVotosCiudadanos} voto${if (reporte.totalVotosCiudadanos != 1) "s" else ""} ciudadano${if (reporte.totalVotosCiudadanos != 1) "s" else ""}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color(0xFF1E293B)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                            Text(
                                "${reporte.votosReales} reales",
                                fontSize = 13.sp,
                                color = Color(0xFF16A34A),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Cancel, null, tint = Color(0xFFE04F5F), modifier = Modifier.size(16.dp))
                            Text(
                                "${reporte.votosFalsos} falsas",
                                fontSize = 13.sp,
                                color = Color(0xFFE04F5F),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
