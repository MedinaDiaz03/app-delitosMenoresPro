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
import com.example.proyectofinal.ui.theme.GreenPrimary

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
    val reports = listOf(
        ReportHistoryItem(
            "Vandalismo", "24 oct, 2023", "14:30",
            "Grafiti en las paredes de la entrada principal del parque comunitario. Se...",
            "Confirmado", Color(0xFF0B6E4F), Icons.Default.Edit,
            hasImage = true
        ),
        ReportHistoryItem(
            "Robo", "22 oct, 2023", "21:15",
            "Intento de robo de bicicleta cerca de los estantes del metro. El perpetrador huyó...",
            "En Revisión", Color(0xFFF2994A), Icons.Default.Person,
        ),
        ReportHistoryItem(
            "Accidente", "19 oct, 2023", "08:45",
            "Colisión menor entre dos vehículos en la intersección de 5ta y Main. Sin heridos.",
            "Rechazado", Color(0xFFEB5757), Icons.Default.DirectionsCar,
            alertMessage = "Reporte duplicado ya atendido por las autoridades."
        ),
        ReportHistoryItem(
            "Problema de Servicios", "15 oct, 2023", "19:00",
            "Toda una cuadra de luces de la calle parpadeando y apagándose...",
            "Confirmado", Color(0xFF0B6E4F), Icons.Default.Lightbulb
        )
    )

    Scaffold(
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
                        Icon(Icons.Default.NotificationsNone, contentDescription = "Notifications")
                    }
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Gray)
                        ) {
                            Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.padding(4.dp))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
                .background(Color(0xFFF8F9FA))
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
                    color = Color.Gray,
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
                    reports
                } else {
                    reports.filter { it.status == selectedFilter }
                }

                if (filteredReports.isEmpty()) {
                    EmptyHistoryView(navController)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredReports) { report ->
                            ReportCard(report)
                        }
                    }
                }
            }
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
                    .background(Color(0xFFE6F4F1).copy(alpha = 0.5f))
            )
            
            // Main Icon Container
            Card(
                modifier = Modifier.size(120.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
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
            color = Color(0xFF1A1C1E)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tu historial está vacío. Sé el primero en alertar a tus vecinos y contribuye activamente a la seguridad y bienestar de tu comunidad.",
            fontSize = 15.sp,
            color = Color.Gray,
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
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
