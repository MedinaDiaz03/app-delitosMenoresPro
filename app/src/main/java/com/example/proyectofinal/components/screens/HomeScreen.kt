package com.example.proyectofinal.components.screens

import android.widget.Toast
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.proyectofinal.repositorios.ReporteRepositorio
import com.example.proyectofinal.ui.theme.GreenPrimary
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val CAJAMARCA = LatLng(-7.1638, -78.5001)

private fun obtenerColorMarker(categoria: String): Float {
    return when (categoria.lowercase()) {
        "robo" -> BitmapDescriptorFactory.HUE_RED
        "vandalismo" -> BitmapDescriptorFactory.HUE_ORANGE
        "pelea" -> BitmapDescriptorFactory.HUE_YELLOW
        "drogas" -> BitmapDescriptorFactory.HUE_VIOLET
        "acoso" -> BitmapDescriptorFactory.HUE_ROSE
        "infraestructura" -> BitmapDescriptorFactory.HUE_AZURE
        else -> BitmapDescriptorFactory.HUE_RED
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val colores = MaterialTheme.colorScheme
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val authRepositorio = remember { AutenticacionRepositorio() }
    val reporteRepositorio = remember { ReporteRepositorio() }
    val locationRepositorio = remember { LocationRepositorio(context) }

    var usuario by remember { mutableStateOf<Usuario?>(null) }
    var reportes by remember { mutableStateOf<List<Reporte>>(emptyList()) }
    var mapaOscuro by remember { mutableStateOf(false) }
    var marcadoresVisibles by remember { mutableStateOf(true) }

    // Animación de parpadeo para los reportes
    LaunchedEffect(Unit) {
        while (true) {
            marcadoresVisibles = !marcadoresVisibles
            delay(800)
        }
    }

    // Permiso de ubicación — lo pedimos aquí en el Home
    val locationPermission = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    val camaraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(CAJAMARCA, 13f)
    }

    // Pedir permiso al entrar a la pantalla
    LaunchedEffect(Unit) {
        val datosUsuario = authRepositorio.obtenerDatosUsuarioActual()
        usuario = datosUsuario

        // DINÁMICO: Solo los policías cargan y ven los reportes activos
        if (datosUsuario?.rol == "policia") {
            reportes = reporteRepositorio.obtenerReportes().filter { it.estado == "activo" }
        } else {
            reportes = emptyList()
        }

        if (!locationPermission.status.isGranted) {
            locationPermission.launchPermissionRequest()
        }
    }

    // Mover cámara cuando se otorga el permiso
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
                // Si el GPS tarda en responder, evitamos que no haga nada:
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
                    // Botón X para cerrar el drawer
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
                    esPolicia = usuario?.rol == "policia",
                    marcadoresVisibles = marcadoresVisibles,
                    navController = navController
                )

                // Tarjeta de estado arriba
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    SecurityStatusCard()
                    Spacer(modifier = Modifier.height(16.dp))
                    FilterChipsRow()
                }

                // Botones flotantes derecha — solo 2 ahora
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botón 1: cambiar tema del mapa (claro/oscuro)
                    MapActionButton(
                        icon = if (mapaOscuro) Icons.Default.LightMode else Icons.Default.DarkMode,
                        onClick = { mapaOscuro = !mapaOscuro }
                    )
                    // Botón 2: volver a mi ubicación
                    MapActionButton(
                        icon = Icons.Default.MyLocation,
                        onClick = { scope.launch { volverAMiUbicacion() } }
                    )
                }

                // Botón SOS
                BotonSOS(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapaConReportes(
    reportes: List<Reporte>,
    camaraState: com.google.maps.android.compose.CameraPositionState,
    locationGranted: Boolean,
    mapaOscuro: Boolean,
    esPolicia: Boolean,
    marcadoresVisibles: Boolean,
    navController: NavController
) {
    val context = LocalContext.current // Necesitamos el contexto para leer el archivo

    val propiedadesMapa = MapProperties(
        isMyLocationEnabled = locationGranted,
        mapStyleOptions = if (mapaOscuro) {
            // Carga el archivo .json de forma segura
            MapStyleOptions.loadRawResourceStyle(context, com.example.proyectofinal.R.raw.map_dark_style)
        } else null
    )

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = camaraState,
        properties = propiedadesMapa,
        uiSettings = MapUiSettings(myLocationButtonEnabled = false)
    ) {
        if (esPolicia && marcadoresVisibles) {
            reportes.forEach { reporte ->
                if (reporte.latitud != 0.0 && reporte.longitud != 0.0) {
                    val color = obtenerColorMarker(reporte.categoria)
                    Marker(
                        state = MarkerState(
                            position = LatLng(reporte.latitud, reporte.longitud)
                        ),
                        title = "Reporte",
                        snippet = reporte.categoria,
                        icon = BitmapDescriptorFactory.defaultMarker(color),
                        onClick = {
                            navController.navigate("report_detail/${reporte.id}")
                            true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SecurityStatusCard() {
    val colores = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(0.65f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colores.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Mapa de Seguridad", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colores.onSurface)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colores.primary))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Estado: Seguro", fontSize = 14.sp, color = colores.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { 0.7f },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                color = colores.primary,
                trackColor = colores.outlineVariant
            )
        }
    }
}

@Composable
fun FilterChipsRow() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChipTemplate("Todo", Icons.Default.FilterList, true)
        FilterChipTemplate("Hospitales", Icons.Default.LocalHospital, false)
        FilterChipTemplate("Alertas", Icons.Default.Report, false)
    }
}

@Composable
fun FilterChipTemplate(text: String, icon: ImageVector, isSelected: Boolean) {
    val colores = MaterialTheme.colorScheme
    Surface(
        color = if (isSelected) colores.primary else colores.surface,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp,
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, colores.outlineVariant) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) Color.White else colores.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                color = if (isSelected) Color.White else colores.onSurfaceVariant
            )
        }
    }
}

// MapActionButton ahora recibe un onClick
@Composable
fun MapActionButton(icon: ImageVector, onClick: () -> Unit = {}) {
    val colores = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.size(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = colores.surface,
        shadowElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, colores.outlineVariant),
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = colores.primary)
        }
    }
}

// ─── BOTÓN SOS ─────────────────────────────────────────────────────────────────
// Lógica simple: mantén presionado 1 segundo → llama al 105 (emergencias Perú)
// Un toque corto muestra un aviso para evitar llamadas accidentales

@Composable
fun BotonSOS(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var mostrarDialogo by remember { mutableStateOf(false) }
    var confirmacion by remember { mutableStateOf(false) }

    // Diálogo de confirmación antes de llamar
    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            icon = { Icon(Icons.Default.LocalPolice, null, tint = Color(0xFFE04F5F), modifier = Modifier.size(36.dp)) },
            title = { Text("¿Llamar a emergencias?", fontWeight = FontWeight.Bold) },
            text  = { Text("Se marcará el número 105 (Policía Nacional del Perú). ¿Confirmas la llamada?") },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogo = false
                        // Lanzar la llamada — requiere permiso CALL_PHONE en el Manifest
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:105"))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE04F5F))
                ) {
                    Text("Llamar ahora", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogo = false
                    confirmacion=true
                }) {
                    Text("Cancelar")
                }
            }

        )
    }
    else if (confirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            icon = { Icon(Icons.Default.LocalPolice, null, tint = Color(0xFFE04F5F), modifier = Modifier.size(36.dp)) },
            title = { Text("¿estás seguro(a) que quieres cancelar la llamada?", fontWeight = FontWeight.Bold) },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogo = false
                        // Lanzar la llamada — requiere permiso CALL_PHONE en el Manifest
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:105"))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE04F5F))
                ) {
                    Text("Llamar ahora", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("si estoy seguro")
                }
            }

        )
    }

    Button(
        onClick = { mostrarDialogo = true },
        shape = CircleShape,
        modifier = modifier.size(72.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE04F5F)),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.LocalPolice, null, tint = Color.White, modifier = Modifier.size(22.dp))
            Text("SOS", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
        }
    }
}