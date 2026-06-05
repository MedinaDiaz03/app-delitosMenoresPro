package com.example.proyectofinal.components.screens.HistorialScreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.proyectofinal.modelos.Reporte
import com.example.proyectofinal.repositorios.AutenticacionRepositorio
import com.example.proyectofinal.repositorios.ReporteRepositorio
import com.example.proyectofinal.ui.theme.GreenPrimary
import java.text.SimpleDateFormat
import java.util.*

// ─── PANTALLA PRINCIPAL ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialRepoScreen(navController: NavController) {
    // Estado simple: lista de reportes + loading
    val reportRepositorio = remember { ReporteRepositorio() }
    val authRepositorio   = remember { AutenticacionRepositorio() }
    var reportes  by remember { mutableStateOf<List<Reporte>>(emptyList()) }
    var cargando  by remember { mutableStateOf(true) }
    var filtro    by remember { mutableStateOf("Todos") }
    val colores = MaterialTheme.colorScheme

    // Cargar reportes del usuario al entrar
    LaunchedEffect(Unit) {
        val usuario = authRepositorio.obtenerDatosUsuarioActual()
        if (usuario != null) {
            reportes = reportRepositorio.obtenerReportesPorUsuario(usuario.uid)
        }
        cargando = false
    }

    // Filtrar por categoría (si no es "Todos")
    val reportesFiltrados = if (filtro == "Todos") reportes
    else reportes.filter { it.categoria == filtro }

    Scaffold(
        containerColor = colores.background,
        topBar = { HistorialTopBar(navController) },
        bottomBar = { HistorialBottomBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(colores.background)
        ) {
            // Título
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    "Mi Historial de\nReportes",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 34.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Rastrea tus contribuciones a la comunidad",
                    color = colores.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }

            // Chips de filtro
            val filtros = listOf("Todos", "Robo", "Vandalismo", "Pelea", "Drogas", "Acoso", "Infraestructura")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtros) { f ->
                    FilterChip(
                        selected = filtro == f,
                        onClick = { filtro = f },
                        label = { Text(f) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenPrimary,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Contenido
            when {
                cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
                reportesFiltrados.isEmpty() -> HistorialVacioView(navController)
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reportesFiltrados) { reporte ->
                        ReportCard(reporte, navController)
                    }
                }
            }
        }
    }
}

// ─── TARJETA DE REPORTE CON IMAGEN ────────────────────────────────────────────

@Composable
fun ReportCard(reporte: Reporte, navController: NavController) {
    val colores = MaterialTheme.colorScheme
    val fecha = formatearFecha(reporte.fecha)
    val tieneImagen = !reporte.fotoUrl.isNullOrEmpty() && reporte.fotoUrl.startsWith("http")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Navegamos pasando el ID por la ruta — más estable que savedStateHandle
                navController.navigate("report_detail/${reporte.id}")
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colores.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // ── Imagen de evidencia (ocupa todo el ancho si existe) ──
            if (tieneImagen) {
                Image(
                    painter = rememberAsyncImagePainter(reporte.fotoUrl),
                    contentDescription = "Evidencia del reporte",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // ── Info del reporte ──
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ícono de categoría (solo si no hay imagen)
                if (!tieneImagen) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(GreenPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Report, null, tint = GreenPrimary, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            reporte.categoria.ifEmpty { "Incidente" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Surface(
                            color = GreenPrimary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                "Enviado",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        reporte.descripcion.ifEmpty { "Sin descripción" },
                        fontSize = 13.sp,
                        color = colores.onSurfaceVariant,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Schedule, null, tint = colores.onSurfaceVariant, modifier = Modifier.size(12.dp))
                        Text(
                            fecha,
                            fontSize = 11.sp,
                            color = colores.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        if (tieneImagen) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.Image, null, tint = GreenPrimary, modifier = Modifier.size(12.dp))
                            Text("Foto", fontSize = 11.sp, color = GreenPrimary)
                        }
                    }
                }
            }
        }
    }
}

// ─── VISTA VACÍA ──────────────────────────────────────────────────────────────

@Composable
fun HistorialVacioView(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(GreenPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.History, null, tint = GreenPrimary, modifier = Modifier.size(64.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Sin reportes aún",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Sé el primero en alertar a tu comunidad y contribuir a la seguridad del barrio.",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { navController.navigate("report") },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Hacer primer reporte", fontWeight = FontWeight.Bold)
        }
    }
}

// ─── HELPERS ──────────────────────────────────────────────────────────────────

fun formatearFecha(fecha: com.google.firebase.Timestamp?): String {
    return try {
        if (fecha != null) SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault()).format(fecha.toDate())
        else "Fecha no disponible"
    } catch (_: Exception) { "Fecha no disponible" }
}

// ─── TOP BAR ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialTopBar(navController: NavController) {
    val colores = MaterialTheme.colorScheme
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, null, tint = GreenPrimary, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("SafetyConnect", color = GreenPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = GreenPrimary)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = colores.background)
    )
}

// ─── BOTTOM BAR ───────────────────────────────────────────────────────────────

@Composable
fun HistorialBottomBar(navController: NavController) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("home") },
            icon = { Icon(Icons.Default.Map, "Mapa") },
            label = { Text("Mapa") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("report") },
            icon = { Icon(Icons.Default.AddCircle, "Reportar") },
            label = { Text("Reportar") }
        )
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.History, "Historial") },
            label = { Text("Historial") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("profile") },
            icon = { Icon(Icons.Default.Person, "Perfil") },
            label = { Text("Perfil") }
        )
    }
}