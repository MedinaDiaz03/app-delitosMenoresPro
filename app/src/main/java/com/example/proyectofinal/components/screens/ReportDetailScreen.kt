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
import androidx.compose.runtime.Composable
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
import com.example.proyectofinal.ui.theme.GreenPrimary
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(navController: NavController, reporte: Reporte) {
    val colores = MaterialTheme.colorScheme

    // CANDADO DE SEGURIDAD PARA FECHA Y HORA (Evita crasheos por datos corruptos de Firebase)
    val fechaYHoraStr = try {
        if (reporte.fecha != null) {
            val sdf = SimpleDateFormat("dd MMM, yyyy • HH:mm", Locale.getDefault())
            sdf.format(reporte.fecha.toDate())
        } else {
            "Fecha no disponible"
        }
    } catch (e: Exception) {
        "Fecha no disponible" // Si falla la conversión, no se cae la app
    }

    Scaffold(
        containerColor = colores.background,
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Incidente", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = GreenPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colores.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // CANDADO PARA LA FOTO: Solo intenta renderizar si la URL es textualmente válida
            val tieneFotoValida = !reporte.fotoUrl.isNullOrEmpty() && reporte.fotoUrl.contains("http")

            if (tieneFotoValida) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(reporte.fotoUrl),
                        contentDescription = "Evidencia fotográfica",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(colores.surfaceVariant, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "El reporte no incluye evidencia fotográfica",
                        color = colores.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }

            // ENCABEZADO: CATEGORÍA DEL INCIDENTE
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = (reporte.categoria ?: "INCIDENTE").uppercase(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GreenPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.CalendarMonth, null, tint = colores.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Text(text = fechaYHoraStr, fontSize = 13.sp, color = colores.onSurfaceVariant)
                }
            }

            HorizontalDivider(color = colores.outlineVariant.copy(alpha = 0.5f))

            // METADATOS SEGUROS
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Quién reportó
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier.size(36.dp).background(colores.surfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text("Reportado por", fontSize = 11.sp, color = colores.onSurfaceVariant)
                        Text((reporte.usuarioNombre ?: "Vecino de la comunidad").ifEmpty { "Vecino de la comunidad" }, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }

                // Coordenadas Geográficas
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier.size(36.dp).background(colores.surfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocationOn, null, tint = Color(0xFFE04F5F), modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text("Ubicación del suceso", fontSize = 11.sp, color = colores.onSurfaceVariant)
                        Text("Lat: ${reporte.latitud ?: 0.0}  •  Long: ${reporte.longitud ?: 0.0}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            HorizontalDivider(color = colores.outlineVariant.copy(alpha = 0.5f))

            // SECCIÓN DE LA DESCRIPCIÓN BLINDADA
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Detalle de los hechos", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colores.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, colores.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = (reporte.descripcion ?: "El usuario no añadió descripciones adicionales.").ifEmpty { "El usuario no añadió descripciones adicionales." },
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = colores.onSurface
                    )
                }
            }
        }
    }
}