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
import com.example.proyectofinal.servicios.NotificationHelper
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth

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
    val userFirebase = remember { FirebaseAuth.getInstance().currentUser }
    val photoUrl = userFirebase?.photoUrl

    var usuario by remember { mutableStateOf<Usuario?>(null) }
    var reportes by remember { mutableStateOf<List<Reporte>>(emptyList()) }
    var mapaOscuro by remember { mutableStateOf(false) }
    var marcadoresVisibles by remember { mutableStateOf(true) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var mostrarIconos by remember { mutableStateOf(true) }
    var soloHoy by remember { mutableStateOf(false) }
    val idsNotificados = remember { mutableSetOf<String>() }

    val inicioDeHoy = remember {
        java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    // Animación de parpadeo para los reportes
    var parpadeoVisible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            parpadeoVisible = !parpadeoVisible
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
        
        // Escuchar reportes en tiempo real
        reporteRepositorio.escucharReportes { lista ->
            reportes = lista.filter { it.estado == "activo" }
        }

        if (!locationPermission.status.isGranted) {
            locationPermission.launchPermissionRequest()
        }

        // Loop de notificaciones para el Policía (1km de radio)
        while (true) {
            if (locationPermission.status.isGranted) {
                val ubicacionActual = locationRepositorio.obtenerUbicacionActual()
                if (ubicacionActual != null) {
                    reportes.filter { repo ->
                        repo.id !in idsNotificados &&
                        (repo.fecha?.toDate()?.time ?: 0L) >= inicioDeHoy &&
                        calcularDistancia(
                            ubicacionActual.latitude, ubicacionActual.longitude,
                            repo.latitud, repo.longitud
                        ) < 1000.0
                    }.forEach { repo ->
                        idsNotificados.add(repo.id)
                        NotificationHelper.mostrarNotificacion(
                            context,
                            "NUEVO REPORTE (Policía): ${repo.categoria.uppercase()}",
                            repo.descripcion.ifEmpty { "Se ha detectado un incidente en su zona de patrullaje" }
                        )
                    }
                }
            }
            delay(30000) // Verifica cada 30 segundos
        }
    }

    LaunchedEffect(locationPermission.status.isGranted) {
        if (locationPermission.status.isGranted) {
            val ubicacion = locationRepositorio.obtenerUbicacionActual()
            if (ubicacion != null) {
                userLocation = LatLng(ubicacion.latitude, ubicacion.longitude)
                camaraState.animate(
                    CameraUpdateFactory.newLatLngZoom(userLocation!!, 15f)
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
                drawerContainerColor = Color.White,
                modifier = Modifier.width(300.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))
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
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (photoUrl != null) {
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    null,
                                    modifier = Modifier.size(36.dp),
                                    tint = Color.White
                                )
                            }
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
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                "Oficial Verificado",
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "ACTIVIDAD",
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF2563EB).copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )

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
                        unselectedIconColor = Color(0xFF2563EB),
                        unselectedTextColor = Color(0xFF1E3A8A)
                    )
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Public, null) },
                    label = { Text("Historial Global") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("historial_global")
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = Color(0xFF2563EB),
                        unselectedTextColor = Color(0xFF1E3A8A)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "MI CUENTA",
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF2563EB).copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
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
                        unselectedIconColor = Color(0xFF2563EB),
                        unselectedTextColor = Color(0xFF1E3A8A)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, null, tint = Color.White, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SafetyConnect", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("profile") }) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (photoUrl != null) {
                                    AsyncImage(
                                        model = photoUrl,
                                        contentDescription = "Foto de perfil",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Person,
                                        null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E3A8A))
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
                    marcadoresVisibles = mostrarIconos && parpadeoVisible,
                    navController = navController,
                    userLocation = userLocation,
                    soloHoy = soloHoy
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ControlesMapaCard(
                        mostrarIconos = mostrarIconos,
                        onMostrarIconosChange = { mostrarIconos = it },
                        soloHoy = soloHoy,
                        onSoloHoyChange = { soloHoy = it }
                    )
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
            }
        }
    }
}
