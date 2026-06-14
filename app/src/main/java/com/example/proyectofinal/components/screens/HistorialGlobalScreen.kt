package com.example.proyectofinal.components.screens

import androidx.compose.foundation.background
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

    fun obtenerHorario(timestamp: Long): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return when (calendar.get(java.util.Calendar.HOUR_OF_DAY)) {
            in 6..11 -> "Mañana"
            in 12..17 -> "Tarde"
            in 18..23 -> "Noche"
            else -> "Madrugada"
        }
    }

    var rangoSeleccionado by remember { mutableStateOf("Todo") }

    fun filtrarReportes() {
        reportesFiltrados = reportesOriginales.filter { reporte ->
            val matchCategoria = categoriaSeleccionada == "Todas" ||
                    reporte.categoria.equals(categoriaSeleccionada, true)
            val fechaMs = reporte.fecha.toDate().time
            val matchHorario = horarioSeleccionado == "Todos" ||
                    obtenerHorario(fechaMs) == horarioSeleccionado
            val matchFecha = when {
                fechaInicio != null && fechaFin != null -> fechaMs in fechaInicio!!..fechaFin!!
                fechaInicio != null -> fechaMs >= fechaInicio!!
                fechaFin != null -> fechaMs <= fechaFin!!
                else -> true
            }
            matchCategoria && matchHorario && matchFecha
        }
    }

    LaunchedEffect(Unit) {
        val usuario = authRepo.obtenerDatosUsuarioActual()
        esPolicia = usuario?.rol == "policia"
        repo.escucharReportes { todos ->
            // Ahora incluimos todos los reportes (incluyendo los propios) para evitar confusión
            reportesOriginales = todos
            reportesFiltrados = todos
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Historial Global",
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
        },
        bottomBar = { BottomNavigationBar(navController, esPolicia = esPolicia) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {

            Spacer(modifier = Modifier.height(8.dp))

            // ── FILTROS ──
            Text(
                "Filtros",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(10.dp))

            DropdownMenuBox(
                label = "Categoría",
                opciones = listOf("Todas", "Robo", "Vandalismo", "Pelea", "Drogas", "Acoso", "Infraestructura"),
                seleccion = categoriaSeleccionada,
                onSelected = { categoriaSeleccionada = it; filtrarReportes() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            DropdownMenuBox(
                label = "Horario",
                opciones = listOf("Todos", "Mañana", "Tarde", "Noche", "Madrugada"),
                seleccion = horarioSeleccionado,
                onSelected = { horarioSeleccionado = it; filtrarReportes() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Rango de fecha",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF334155)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Hoy" to { 
                        val cal = java.util.Calendar.getInstance()
                        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                        cal.set(java.util.Calendar.MINUTE, 0)
                        cal.set(java.util.Calendar.SECOND, 0)
                        cal.set(java.util.Calendar.MILLISECOND, 0)
                        fechaInicio = cal.timeInMillis
                        fechaFin = null
                        rangoSeleccionado = "Hoy"
                    },
                    "Semana" to { 
                        val cal = java.util.Calendar.getInstance()
                        cal.add(java.util.Calendar.DAY_OF_YEAR, -7)
                        fechaInicio = cal.timeInMillis
                        fechaFin = null
                        rangoSeleccionado = "Semana"
                    },
                    "Todo" to { 
                        fechaInicio = null
                        fechaFin = null
                        rangoSeleccionado = "Todo"
                    }
                ).forEach { (label, action) ->
                    val isSelected = rangoSeleccionado == label
                    OutlinedButton(
                        onClick = { action(); filtrarReportes() },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) Color(0xFF1E3A8A) else Color.Transparent,
                            contentColor = if (isSelected) Color.White else Color(0xFF1E3A8A)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, Color(0xFF1E3A8A).copy(alpha = 0.4f)
                        )
                    ) {
                        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                reportesOriginales.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                reportesFiltrados.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Sin reportes con estos filtros",
                        color = Color(0xFF475569),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(reportesFiltrados) { reporte ->
                        ReporteItem(reporte) {
                            navController.navigate("report_detail/${reporte.id}")
                        }
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
fun ReporteItem(reporte: Reporte, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = reporte.categoria,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = reporte.estado.uppercase(),
                        color = when (reporte.estado) {
                            "activo" -> Color(0xFFE04F5F)
                            "resuelto" -> Color(0xFF16A34A)
                            else -> Color(0xFF64748B)
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (reporte.descripcion.isEmpty()) "Sin descripción" else reporte.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    color = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Por: ${reporte.usuarioNombre}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFCBD5E1)
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
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF334155)
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF1E293B),
                containerColor = Color.White
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(seleccion, fontSize = 14.sp, color = Color(0xFF1E293B))
                Icon(Icons.Default.ArrowDropDown, null, tint = Color(0xFF64748B))
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = Color(0xFFBFDBFE)
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion, color = Color(0xFF1E293B)) },
                    onClick = { onSelected(opcion); expanded = false }
                )
            }
        }
    }
}
