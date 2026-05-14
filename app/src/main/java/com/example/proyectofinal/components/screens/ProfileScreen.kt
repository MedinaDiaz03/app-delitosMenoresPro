package com.example.proyectofinal.components.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyectofinal.components.navigation.BottomNavigationBar
import com.example.proyectofinal.modelos.Usuario
import com.example.proyectofinal.repositorios.AutenticacionRepositorio
import com.example.proyectofinal.repositorios.ReporteRepositorio
import com.example.proyectofinal.ui.theme.GreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val repositorio = remember { AutenticacionRepositorio() }
    val reporteRepositorio = remember { ReporteRepositorio() }
    var usuario by remember { mutableStateOf<Usuario?>(null) }
    var conteoReportes by remember { mutableIntStateOf(0) }
    
    // SOPORTE MODO OSCURO: Definimos el esquema de colores dinámico
    val colores = MaterialTheme.colorScheme

    LaunchedEffect(Unit) {
        val datosUsuario = repositorio.obtenerDatosUsuarioActual()
        usuario = datosUsuario
        if (datosUsuario != null) {
            conteoReportes = reporteRepositorio.obtenerConteoReportesUsuario(datosUsuario.id)
        }
    }

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
                            "Mi Perfil",
                            color = GreenPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                // BOTÓN ATRÁS: Navegación simple al stack anterior
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Atrás", 
                            tint = GreenPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        repositorio.cerrarSesion()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                        Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar Sesión", tint = Color.Red)
                    }
                },
                // SOPORTE MODO OSCURO: Adaptación automática de la barra superior
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colores.surface)
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                // SOPORTE MODO OSCURO: Fondo dinámico para toda la pantalla
                .background(colores.background)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Cabecera de Perfil
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(3.dp, GreenPrimary, CircleShape)
                        // SOPORTE MODO OSCURO: Superficie variante para el fondo del avatar
                        .background(colores.surfaceVariant)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        tint = colores.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // MOSTRAR DATOS: Se eliminó el texto "Cargando..."
            Text(
                text = if (usuario != null) "${usuario?.nombres} ${usuario?.apellidos}" else "Usuario",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                // SOPORTE MODO OSCURO: Color de texto primario adaptable
                color = colores.onBackground
            )
            Text(
                text = usuario?.correo ?: "---",
                fontSize = 14.sp,
                color = colores.onSurfaceVariant
            )
            Text(
                text = "DNI: ${usuario?.dni ?: "No disponible"}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colores.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tarjeta de Nivel de Confianza
            TrustLevelCard(
                puntos = usuario?.reportesValidados ?: 0,
                nivel = if ((usuario?.reportesValidados ?: 0) > 10) "Experto" else "Nuevo"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tarjetas de Estadísticas
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Reportes Realizados",
                    value = conteoReportes.toString(),
                    icon = Icons.Default.Campaign,
                    containerColor = GreenPrimary,
                    onClick = { navController.navigate("historial_repo") }
                )
                StatCard(
                    title = "Ayuda Comunitaria", 
                    value = "0", 
                    icon = Icons.Default.Handshake, 
                    containerColor = colores.surface,
                    iconColor = GreenPrimary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Contactos de Emergencia
            EmergencyContactsSection()

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun TrustLevelCard(puntos: Int, nivel: String) {
    val colores = MaterialTheme.colorScheme
    val progreso = (puntos.toFloat() / 20f).coerceIn(0.1f, 1f)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        // SOPORTE MODO OSCURO: Card adaptable
        colors = CardDefaults.cardColors(containerColor = colores.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "NIVEL DE CONFIANZA",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colores.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progreso },
                    modifier = Modifier.size(100.dp),
                    color = GreenPrimary,
                    strokeWidth = 10.dp,
                    trackColor = colores.surfaceVariant
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$puntos", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = colores.onSurface)
                    Text(nivel, fontSize = 12.sp, color = colores.onSurfaceVariant)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Puntos por reportes validados", fontSize = 14.sp, color = colores.onSurfaceVariant)
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    iconBgColor: Color? = null,
    iconColor: Color = Color.White,
    onClick: () -> Unit = {}
) {
    val colores = MaterialTheme.colorScheme
    val esPrimario = containerColor == GreenPrimary
    
    // SOPORTE MODO OSCURO: Lógica de colores que respeta el tema oscuro
    val colorContenedor = if (esPrimario) GreenPrimary else colores.surface
    val colorTexto = if (esPrimario) Color.White else colores.onSurface
    val colorIconoFondo = iconBgColor ?: (if (esPrimario) Color(0xFF095A41) else colores.primaryContainer)
    val colorIcono = if (esPrimario) Color.White else GreenPrimary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = colorContenedor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorIconoFondo),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = colorIcono)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, color = colorTexto.copy(alpha = 0.7f))
                Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colorTexto)
            }
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = colorTexto.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun EmergencyContactsSection() {
    val colores = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Contactos de Emergencia", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colores.onBackground)
            Text("Añadir", color = GreenPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { 
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        // SOPORTE MODO OSCURO: Fondo adaptable para los items de la lista
                        .background(colores.surface)
                        .border(1.dp, colores.outlineVariant, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Add, null, tint = colores.onSurfaceVariant)
                        Text("Nuevo", fontSize = 12.sp, color = colores.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
