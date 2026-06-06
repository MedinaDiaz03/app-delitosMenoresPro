package com.example.proyectofinal.components.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyectofinal.components.navigation.BottomNavigationBar
import com.example.proyectofinal.modelos.Reporte
import com.example.proyectofinal.repositorios.AutenticacionRepositorio
import com.example.proyectofinal.repositorios.ReporteRepositorio
import com.example.proyectofinal.ui.theme.GreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialGlobalScreen(navController: NavController) {

    val repo = remember { ReporteRepositorio() }
    val authRepo = remember { AutenticacionRepositorio() }
    var reportesOriginales by remember { mutableStateOf(listOf<Reporte>()) }
    var reportesFiltrados by remember { mutableStateOf(listOf<Reporte>()) }
    var esPolicia by remember { mutableStateOf(false) }

    var categoriaSeleccionada by remember { mutableStateOf("Todas") }
    var horarioSeleccionado by remember { mutableStateOf("Todos") }
    var fechaInicio by remember { mutableStateOf<Long?>(null) }
    var fechaFin by remember { mutableStateOf<Long?>(null) }

    val colores = MaterialTheme.colorScheme

    fun obtenerHorario(timestamp: Long): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp

        val hora = calendar.get(java.util.Calendar.HOUR_OF_DAY)

        return when (hora) {
            in 6..11 -> "Mañana"
            in 12..17 -> "Tarde"
            in 18..23 -> "Noche"
            else -> "Madrugada"
        }
    }

    fun filtrarReportes() {
        reportesFiltrados = reportesOriginales.filter { reporte ->
            val matchCategoria = categoriaSeleccionada == "Todas" ||
                    reporte.categoria.equals(categoriaSeleccionada, true)

            val fechaReporteMs = reporte.fecha.toDate().time
            val horario = obtenerHorario(fechaReporteMs)
            val matchHorario = horarioSeleccionado == "Todos" ||
                    horario == horarioSeleccionado

            val matchFecha = when {
                fechaInicio != null && fechaFin != null ->
                    fechaReporteMs in fechaInicio!!..fechaFin!!
                fechaInicio != null ->
                    fechaReporteMs >= fechaInicio!!
                fechaFin != null ->
                    fechaReporteMs <= fechaFin!!
                else -> true
            }

            matchCategoria && matchHorario && matchFecha
        }
    }

    LaunchedEffect(Unit) {
        val usuario = authRepo.obtenerDatosUsuarioActual()
        esPolicia = usuario?.rol == "policia"

        repo.obtenerTodosLosReportes {
            reportesOriginales = it
            reportesFiltrados = it
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Historial Global 🌍", color = GreenPrimary, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = GreenPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colores.surface)
            )
        },
        bottomBar = { BottomNavigationBar(navController, esPolicia = esPolicia) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Filtros 🔎", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            DropdownMenuBox(
                label = "Categoría",
                opciones = listOf("Todas", "Robo", "Vandalismo", "Pelea", "Drogas", "Acoso", "Infraestructura"),
                seleccion = categoriaSeleccionada,
                onSelected = {
                    categoriaSeleccionada = it
                    filtrarReportes()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            DropdownMenuBox(
                label = "Horario",
                opciones = listOf("Todos", "Mañana", "Tarde", "Noche", "Madrugada"),
                seleccion = horarioSeleccionado,
                onSelected = {
                    horarioSeleccionado = it
                    filtrarReportes()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Rango de Fecha 📅", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val ahora = System.currentTimeMillis()
                        fechaInicio = ahora - (24 * 60 * 60 * 1000)
                        fechaFin = ahora
                        filtrarReportes()
                    },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("24h", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        val ahora = System.currentTimeMillis()
                        fechaInicio = ahora - (7 * 24 * 60 * 60 * 1000)
                        fechaFin = ahora
                        filtrarReportes()
                    },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Semana", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        fechaInicio = null
                        fechaFin = null
                        filtrarReportes()
                    },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Todo", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (reportesOriginales.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            } else if (reportesFiltrados.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay reportes con estos filtros", color = colores.onSurfaceVariant)
                }
            } else {
                LazyColumn {
                    items(reportesFiltrados) { reporte ->
                        ReporteItem(reporte) {
                            navController.navigate("report_detail/${reporte.id}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReporteItem(reporte: Reporte, onClick: () -> Unit) {
    val colores = MaterialTheme.colorScheme
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = colores.surface),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = GreenPrimary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = reporte.categoria,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = GreenPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = reporte.estado.uppercase(),
                        color = when(reporte.estado) {
                            "activo" -> Color(0xFFE04F5F)
                            "resuelto" -> Color(0xFF4CAF50)
                            else -> colores.onSurfaceVariant
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if(reporte.descripcion.isEmpty()) "Sin descripción" else reporte.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    color = colores.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Por: ${reporte.usuarioNombre}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colores.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colores.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun DropdownMenuBox(
    label: String,
    opciones: List<String>,
    seleccion: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(seleccion, fontSize = 14.sp)
                Icon(Icons.Default.ArrowDropDown, null)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onSelected(opcion)
                        expanded = false
                    }
                )
            }
        }
    }
}
