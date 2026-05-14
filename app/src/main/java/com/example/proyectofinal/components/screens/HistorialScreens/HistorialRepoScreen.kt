package com.example.proyectofinal.components.screens.HistorialScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyectofinal.modelos.Reporte
import com.example.proyectofinal.repositorios.AutenticacionRepositorio
import com.example.proyectofinal.repositorios.ReporteRepositorio
import com.example.proyectofinal.ui.theme.GreenPrimary
import java.text.SimpleDateFormat
import java.util.*

data class ReportHistoryItem(
    val title: String,
    val date: String,
    val time: String,
    val description: String,
    val status: String,
    val statusColor: Color,
    val icon: ImageVector,
    val hasImage: Boolean = false,
    val alertMessage: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialRepoScreen(navController: NavController) {
    val reportRepositorio = remember { ReporteRepositorio() }
    val authRepositorio = remember { AutenticacionRepositorio() }
    var reportes by remember { mutableStateOf<List<Reporte>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    val colores = MaterialTheme.colorScheme

    LaunchedEffect(Unit) {
        val usuario = authRepositorio.obtenerDatosUsuarioActual()
        if (usuario != null) {
            reportes = reportRepositorio.obtenerReportesPorUsuario(usuario.id)
        }
        cargando = false
    }

    Scaffold(
        containerColor = colores.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "SafetyConnect",
                            color = GreenPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GreenPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.NotificationsNone, 
                            contentDescription = "Notifications",
                            tint = colores.onSurface
                        )
                    }
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colores.secondaryContainer)
                        ) {
                            Icon(
                                Icons.Default.Person, 
                                null, 
                                tint = colores.onSecondaryContainer, 
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colores.background,
                    titleContentColor = colores.onBackground
                )
            )
        },
        bottomBar = {
            BottomNavigationBarHistory(navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colores.background)
        ) {
            // Títulos (Con padding horizontal)
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    "Mi Historial de\nReportes",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 34.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Rastrea y gestiona tus contribuciones a la comunidad",
                    color = colores.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Filtros (Sin padding en el contenedor, pero con contentPadding interno)
            val filters = listOf("Todos", "Confirmado", "En Revisión", "Rechazado")
            var selectedFilter by remember { mutableStateOf("Todos") }
            
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp), // Alineación inicial
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenPrimary,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            // Contenido Principal (Padded)
            Column(modifier = Modifier.padding(horizontal = 16.dp).weight(1f)) {
                Spacer(modifier = Modifier.height(8.dp))

                val filteredReports = if (selectedFilter == "Todos") {
                    reportes
                } else {
                    // Por ahora los reportes reales no tienen estado dinámico en el modelo simple, 
                    // se podría filtrar por categoría o añadir estado al modelo Reporte.
                    reportes.filter { it.categoria == selectedFilter }
                }

                if (cargando) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                } else if (filteredReports.isEmpty()) {
                    EmptyHistoryView(navController)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredReports) { report ->
                            RealReportCard(report)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RealReportCard(report: Reporte) {
    val sdfFecha = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
    val sdfHora = SimpleDateFormat("HH:mm", Locale.getDefault())
    val fechaStr = sdfFecha.format(report.fecha.toDate())
    val horaStr = sdfHora.format(report.fecha.toDate())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(GreenPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Report, null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(report.categoria, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("$fechaStr • $horaStr", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(
                    color = GreenPrimary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
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
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(report.descripcion.ifEmpty { "Sin descripción" }, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            
            Spacer(modifier = Modifier.height(8.dp))
            Text("Por: ${report.usuarioNombre}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun EmptyHistoryView(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Illustration Area
        Box(
            modifier = Modifier
                .size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background glow/circle
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
            )
            
            // Main Icon Container
            Card(
                modifier = Modifier.size(120.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = GreenPrimary
                    )
                }
            }

            // "¡Comienza hoy!" Badge
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 10.dp),
                color = Color(0xFFF2994A).copy(alpha = 0.9f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "¡Comienza hoy!",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "No tienes ningún\nreporte",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 34.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tu historial está vacío. Sé el primero en alertar a tus vecinos y contribuye activamente a la seguridad y bienestar de tu comunidad.",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { navController.navigate("report") },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Realizar primer reporte", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = { /* Acción de ayuda */ }) {
            Text(
                "¿Cómo funciona la red de seguridad?",
                color = GreenPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun ReportCard(report: ReportHistoryItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE6F4F1)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(report.icon, null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(report.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("${report.date} • ${report.time}", fontSize = 12.sp, color = Color.Gray)
                }
                Surface(
                    color = report.statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        report.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = report.statusColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(report.description, fontSize = 14.sp, color = Color.DarkGray)
            
            if (report.hasImage) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.LightGray)
                ) {
                    // Placeholder for image
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF2D6A4F).copy(alpha = 0.5f)))
                }
            }
            
            if (report.alertMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color(0xFFFDE8E8),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = Color(0xFFE04F5F), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(report.alertMessage, fontSize = 12.sp, color = Color(0xFFE04F5F), fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBarHistory(navController: NavController) {
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
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.Notifications, "Alertas") },
            label = { Text("Alertas") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("profile") },
            icon = { Icon(Icons.Default.Person, "Perfil") },
            label = { Text("Perfil") }
        )
    }
}
