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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.proyectofinal.modelos.Reporte
import com.example.proyectofinal.repositorios.ReporteRepositorio
import com.example.proyectofinal.ui.theme.GreenPrimary
import java.text.SimpleDateFormat
import java.util.Locale

// ─── PANTALLA DE DETALLE ───────────────────────────────────────────────────────
// Recibe el ID del reporte por navegación y lo carga desde Firestore.
// Así evitamos el crash de pasar objetos por savedStateHandle.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(navController: NavController, reporteId: String) {
    val repositorio = remember { ReporteRepositorio() }
    var reporte by remember { mutableStateOf<Reporte?>(null) }
    var cargando by remember { mutableStateOf(true) }
    val colores = MaterialTheme.colorScheme

    // Carga el reporte por ID desde Firestore
    LaunchedEffect(reporteId) {
        reporte = repositorio.obtenerReportePorId(reporteId)
        cargando = false
    }

    Scaffold(
        containerColor = colores.background,
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Reporte", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = GreenPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colores.background)
            )
        }
    ) { padding ->
        when {
            // Cargando
            cargando -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GreenPrimary)
            }

            // No se encontró el reporte
            reporte == null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No se pudo cargar el reporte.", color = colores.onSurfaceVariant)
            }

            // Mostrar detalle
            else -> DetalleContenido(reporte = reporte!!, padding = padding, colores = colores)
        }
    }
}

// ─── CONTENIDO DEL DETALLE ────────────────────────────────────────────────────

@Composable
fun DetalleContenido(reporte: Reporte, padding: PaddingValues, colores: ColorScheme) {
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
                modifier = Modifier.fillMaxWidth().height(260.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    .height(100.dp)
                    .background(colores.surfaceVariant, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Sin evidencia fotográfica", color = colores.onSurfaceVariant, fontSize = 14.sp)
            }
        }

        // ── CATEGORÍA Y FECHA ──
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                reporte.categoria.uppercase(),
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GreenPrimary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.CalendarMonth, null, tint = colores.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Text(fecha, fontSize = 13.sp, color = colores.onSurfaceVariant)
            }
        }

        HorizontalDivider(color = colores.outlineVariant.copy(alpha = 0.5f))

        // ── QUIÉN REPORTÓ ──
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(36.dp).background(colores.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
            }
            Column {
                Text("Reportado por", fontSize = 11.sp, color = colores.onSurfaceVariant)
                Text(
                    reporte.usuarioNombre.ifEmpty { "Vecino de la comunidad" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // ── UBICACIÓN ──
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(36.dp).background(colores.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocationOn, null, tint = Color(0xFFE04F5F), modifier = Modifier.size(18.dp))
            }
            Column {
                Text("Ubicación del suceso", fontSize = 11.sp, color = colores.onSurfaceVariant)
                Text(
                    "Lat: ${reporte.latitud}  •  Long: ${reporte.longitud}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        HorizontalDivider(color = colores.outlineVariant.copy(alpha = 0.5f))

        // ── DESCRIPCIÓN ──
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Detalle de los hechos", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colores.surface),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, colores.outlineVariant.copy(alpha = 0.5f))
            ) {
                Text(
                    text = reporte.descripcion.ifEmpty { "El usuario no añadió descripción." },
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = colores.onSurface
                )
            }
        }
    }
}