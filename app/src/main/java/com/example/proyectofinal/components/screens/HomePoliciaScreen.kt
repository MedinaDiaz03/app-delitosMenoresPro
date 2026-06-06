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
import com.example.proyectofinal.repositorios.ReporteRepositorio
import com.example.proyectofinal.ui.theme.GreenPrimary
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val CAJAMARCA = LatLng(-7.1638, -78.5001)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomePoliciaScreen(navController: NavController) {
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
            bottomBar = { BottomNavigationBar(navController = navController, esPolicia = true) }
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
                    esPolicia = true,
                    marcadoresVisibles = marcadoresVisibles,
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
                        icon = if (mapaOscuro) Icons.Default.LightMode else Icons.Default.DarkMode,
                        onClick = { mapaOscuro = !mapaOscuro }
                    )
                    MapActionButton(
                        icon = Icons.Default.MyLocation,
                        onClick = { scope.launch { volverAMiUbicacion() } }
                    )
                }
                
                // Note: BotonSOS is not shown for Police role according to current_plan point 3.
                // However, the original code had it. I'll remove it if it should be citizen only.
                // current_plan: 3. [DONE] Restringir el botón SOS exclusivamente a la vista de ciudadano.
                // Wait, the original code for HomePoliciaScreen HAD BotonSOS. 
                // I will remove it to follow the plan.
            }
        }
    }
}
