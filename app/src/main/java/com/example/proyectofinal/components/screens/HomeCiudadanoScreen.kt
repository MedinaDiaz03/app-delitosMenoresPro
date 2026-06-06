package com.example.proyectofinal.components.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyectofinal.components.navigation.BottomNavigationBar
import com.example.proyectofinal.modelos.Reporte
import com.example.proyectofinal.modelos.Usuario
import com.example.proyectofinal.repositorios.AutenticacionRepositorio
import com.example.proyectofinal.repositorios.LocationRepositorio
import com.example.proyectofinal.repositorios.LocationShareRepositorio
import com.example.proyectofinal.repositorios.ReporteRepositorio
import com.example.proyectofinal.ui.theme.GreenPrimary
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

private val CAJAMARCA = LatLng(-7.1638, -78.5001)

fun calcularDistancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371e3 // Radio de la Tierra en metros
    val phi1 = lat1 * PI / 180
    val phi2 = lat2 * PI / 180
    val deltaPhi = (lat2 - lat1) * PI / 180
    val deltaLambda = (lon2 - lon1) * PI / 180

    val a = sin(deltaPhi / 2).pow(2) +
            cos(phi1) * cos(phi2) *
            sin(deltaLambda / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return r * c
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeCiudadanoScreen(navController: NavController) {
    val context = LocalContext.current
    val colores = MaterialTheme.colorScheme
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val authRepositorio = remember { AutenticacionRepositorio() }
    val reporteRepositorio = remember { ReporteRepositorio() }
    val locationRepositorio = remember { LocationRepositorio(context) }
    val shareRepo = remember { LocationShareRepositorio() }

    var usuario by remember { mutableStateOf<Usuario?>(null) }
    var reportes by remember { mutableStateOf<List<Reporte>>(emptyList()) }
    var mapaOscuro by remember { mutableStateOf(false) }
    var compartiendoUbicacion by remember { mutableStateOf(false) }

    // Estado para validación de proximidad
    var mostrarDialogoProximidad by remember { mutableStateOf(false) }
    var reporteCercano by remember { mutableStateOf<Reporte?>(null) }
    var ultimoIdMostrado by remember { mutableStateOf<String?>(null) }

    val locationPermission = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    val camaraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(CAJAMARCA, 13f)
    }

    LaunchedEffect(Unit) {
        usuario = authRepositorio.obtenerDatosUsuarioActual()
        reportes = reporteRepositorio.obtenerReportes().filter { it.estado == "activo" }

        if (!locationPermission.status.isGranted) {
            locationPermission.launchPermissionRequest()
        }

        // Loop de detección de proximidad
        while (true) {
            if (locationPermission.status.isGranted) {
                val ubicacionActual = locationRepositorio.obtenerUbicacionActual()
                if (ubicacionActual != null) {
                    val cercano = reportes.find { repo ->
                        repo.id != ultimoIdMostrado &&
                        calcularDistancia(
                            ubicacionActual.latitude,
                            ubicacionActual.longitude,
                            repo.latitud,
                            repo.longitud
                        ) < 150.0 // 150 metros
                    }

                    if (cercano != null) {
                        reporteCercano = cercano
                        mostrarDialogoProximidad = true
                        ultimoIdMostrado = cercano.id
                    }
                }
            }
            delay(10000) // Verificar cada 10 segundos
        }
    }

    LaunchedEffect(locationPermission.status.isGranted) {
        if (locationPermission.status.isGranted) {
            val ubicacion = locationRepositorio.obtenerUbicacionActual()
            if (ubicacion != null) {
                camaraState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(ubicacion.latitude, ubicacion.longitude),
                        15f
                    )
                )
            }
        }
    }

    LaunchedEffect(compartiendoUbicacion) {
        if (compartiendoUbicacion) {
            val user = authRepositorio.obtenerDatosUsuarioActual()
            if (user != null) {
                repeat(120) { // ~4 minutos
                    val loc = locationRepositorio.obtenerUbicacionActual()
                    if (loc != null) {
                        shareRepo.actualizarUbicacion(user.uid, loc.latitude, loc.longitude)
                    }
                    delay(2000)
                }
                shareRepo.detener(user.uid)
                compartiendoUbicacion = false
            }
        }
    }

    suspend fun volverAMiUbicacion() {
        if (locationPermission.status.isGranted) {
            val ubicacion = locationRepositorio.obtenerUbicacionActual()
            if (ubicacion != null) {
                camaraState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(ubicacion.latitude, ubicacion.longitude),
                        15f
                    )
                )
            } else {
                Toast.makeText(context, "Buscando señal GPS... Reintentando", Toast.LENGTH_SHORT).show()
                camaraState.animate(CameraUpdateFactory.newLatLngZoom(CAJAMARCA, 13f))
            }
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = colores.surface,
                modifier = Modifier.width(300.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(GreenPrimary, GreenPrimary.copy(alpha = 0.8f))
                            )
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    IconButton(
                        onClick = { scope.launch { drawerState.close() } },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar menú", tint = Color.White)
                    }

                    Column {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                Icons.Default.Person,
                                null,
                                modifier = Modifier.fillMaxSize().padding(12.dp),
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = usuario?.nombre ?: "Usuario",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = usuario?.email ?: "",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.History, null) },
                    label = { Text("Historial de reportes") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("historial_repo")
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = GreenPrimary,
                        unselectedTextColor = colores.onSurface
                    )
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Public, null) },
                    label = { Text("Historial Global 🌍") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("historial_global")
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = GreenPrimary,
                        unselectedTextColor = colores.onSurface
                    )
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("Configuración") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("profile")
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = GreenPrimary,
                        unselectedTextColor = colores.onSurface
                    )
                )

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null) },
                    label = { Text("Cerrar Sesión") },
                    selected = false,
                    onClick = {
                        authRepositorio.cerrarSesion()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = Color(0xFFE04F5F),
                        unselectedTextColor = Color(0xFFE04F5F)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("SafetyConnect", color = GreenPrimary, fontWeight = FontWeight.Bold)
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = GreenPrimary)
                        }
                    },
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = GreenPrimary)
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
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = colores.surface)
                )
            },
            bottomBar = { BottomNavigationBar(navController = navController) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                MapaConReportes(
                    reportes = reportes,
                    camaraState = camaraState,
                    locationGranted = locationPermission.status.isGranted,
                    mapaOscuro = mapaOscuro,
                    esPolicia = false,
                    marcadoresVisibles = true,
                    navController = navController
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    SecurityStatusCard()
                    Spacer(modifier = Modifier.height(16.dp))
                    FilterChipsRow()
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MapActionButton(
                        icon = if (compartiendoUbicacion) Icons.Default.GpsFixed else Icons.Default.GpsOff,
                        onClick = {
                            if (!locationPermission.status.isGranted) {
                                locationPermission.launchPermissionRequest()
                            } else {
                                compartiendoUbicacion = !compartiendoUbicacion
                                if (!compartiendoUbicacion) {
                                    scope.launch {
                                        usuario?.let { shareRepo.detener(it.uid) }
                                    }
                                }
                            }
                        }
                    )
                    MapActionButton(
                        icon = if (mapaOscuro) Icons.Default.LightMode else Icons.Default.DarkMode,
                        onClick = { mapaOscuro = !mapaOscuro }
                    )
                    MapActionButton(
                        icon = Icons.Default.MyLocation,
                        onClick = { scope.launch { volverAMiUbicacion() } }
                    )
                }

                BotonSOS(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp))

                // Diálogo de Validación de Proximidad
                if (mostrarDialogoProximidad && reporteCercano != null) {
                    AlertDialog(
                        onDismissRequest = { mostrarDialogoProximidad = false },
                        icon = { Icon(Icons.Default.Help, contentDescription = null, tint = GreenPrimary) },
                        title = { Text("¿Incidente activo?") },
                        text = { 
                            Text("Estás cerca de un reporte de '${reporteCercano?.categoria?.uppercase()}'. ¿Sigue ocurriendo este incidente?") 
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    scope.launch {
                                        reporteCercano?.id?.let { reporteRepositorio.confirmarReporte(it) }
                                        mostrarDialogoProximidad = false
                                        Toast.makeText(context, "Gracias por confirmar", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                            ) {
                                Text("Sí, es real")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        reporteCercano?.id?.let { reporteRepositorio.desmentirReporte(it) }
                                        mostrarDialogoProximidad = false
                                        Toast.makeText(context, "Reporte marcado como no verificado", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text("No ocurre / Falsa alarma", color = Color.Gray)
                            }
                        }
                    )
                }
            }
        }
    }
}
