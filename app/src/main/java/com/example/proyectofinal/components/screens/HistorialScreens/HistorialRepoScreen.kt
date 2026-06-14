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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.proyectofinal.modelos.Reporte
import com.example.proyectofinal.repositorios.AutenticacionRepositorio
import com.example.proyectofinal.repositorios.ReporteRepositorio
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialRepoScreen(navController: NavController) {
    val reportRepositorio = remember { ReporteRepositorio() }
    val authRepositorio = remember { AutenticacionRepositorio() }
    var reportes by remember { mutableStateOf<List<Reporte>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var filtro by remember { mutableStateOf("Todos") }
    var esPolicia by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        cargando = true
        val usuario = authRepositorio.obtenerDatosUsuarioActual()
        if (usuario != null) {
            esPolicia = usuario.rol == "policia"
            val resultado = reportRepositorio.obtenerReportesPorUsuario(usuario.uid)
            reportes = resultado
        }
        cargando = false
    }

    val reportesFiltrados = if (filtro == "Todos") reportes
    else reportes.filter { it.categoria == filtro }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = { HistorialTopBar(navController) },
        bottomBar = { HistorialBottomBar(navController, esPolicia) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(
                    "Mi historial de\nreportes",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 32.sp,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Rastrea tus contribuciones a la comunidad",
                    fontSize = 14.sp,
                    color = Color(0xFF475569)
                )
            }

            val filtros = listOf("Todos", "Robo", "Vandalismo", "Pelea", "Drogas", "Acoso", "Infraestructura")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtros) { f ->
                    FilterChip(
                        selected = filtro == f,
                        onClick = { filtro = f },
                        label = { Text(f, fontWeight = if (filtro == f) FontWeight.SemiBold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Color(0xFF334155)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = filtro == f,
                            borderColor = Color(0xFFCBD5E1),
                            selectedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when {
                cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                reportesFiltrados.isEmpty() -> HistorialVacioView(navController, esPolicia)
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

@Composable
fun ReportCard(reporte: Reporte, navController: NavController) {
    val fecha = formatearFecha(reporte.fecha)
    val tieneImagen = !reporte.fotoUrl.isNullOrEmpty() && reporte.fotoUrl.startsWith("http")

    Card(
        modifier = Modifier.fillMaxWidth().clickable {
            navController.navigate("report_detail/${reporte.id}")
        },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column {
            if (tieneImagen) {
                Image(
                    painter = rememberAsyncImagePainter(reporte.fotoUrl),
                    contentDescription = "Evidencia",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!tieneImagen) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Report, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
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
                            fontSize = 15.sp,
                            color = Color(0xFF1E293B)
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Enviado",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        reporte.descripcion.ifEmpty { "Sin descripción" },
                        fontSize = 13.sp,
                        color = Color(0xFF475569),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Schedule, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
                        Text(fecha, fontSize = 11.sp, color = Color(0xFF94A3B8))
                        if (tieneImagen) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.Image, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                            Text("Foto", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistorialVacioView(navController: NavController, esPolicia: Boolean = false) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.History, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Sin reportes aún", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Sé el primero en alertar a tu comunidad y contribuir a la seguridad del barrio.",
            fontSize = 14.sp,
            color = Color(0xFF475569),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        if (!esPolicia) {
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { navController.navigate("report") },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hacer primer reporte", fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun formatearFecha(fecha: com.google.firebase.Timestamp?): String {
    return try {
        if (fecha != null) SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault()).format(fecha.toDate())
        else "Fecha no disponible"
    } catch (_: Exception) { "Fecha no disponible" }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialTopBar(navController: NavController) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Shield,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "SafetyConnect",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    "Atrás",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E3A8A))
    )
}

@Composable
fun HistorialBottomBar(navController: NavController, esPolicia: Boolean = false) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("home") },
            icon = { Icon(Icons.Default.Map, "Mapa") },
            label = { Text("Mapa") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = Color(0xFF94A3B8),
                unselectedTextColor = Color(0xFF94A3B8),
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        )
        if (!esPolicia) {
            NavigationBarItem(
                selected = false,
                onClick = { navController.navigate("report") },
                icon = { Icon(Icons.Default.AddCircle, "Reportar") },
                label = { Text("Reportar") },
                colors = NavigationBarItemDefaults.colors(
                    unselectedIconColor = Color(0xFF94A3B8),
                    unselectedTextColor = Color(0xFF94A3B8),
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            )
        }
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.History, "Historial") },
            label = { Text("Historial") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("profile") },
            icon = { Icon(Icons.Default.Person, "Perfil") },
            label = { Text("Perfil") },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = Color(0xFF94A3B8),
                unselectedTextColor = Color(0xFF94A3B8),
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        )
    }
}
