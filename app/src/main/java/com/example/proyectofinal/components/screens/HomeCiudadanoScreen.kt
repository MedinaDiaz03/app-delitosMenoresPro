package com.example.proyectofinal.components.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import android.content.Intent
import android.net.Uri
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
import com.example.proyectofinal.servicios.NotificationHelper
import com.example.proyectofinal.util.DistanceUtils
import com.example.proyectofinal.util.MapConstants
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val locationShareRepositorio = remember { LocationShareRepositorio() }
    val userFirebase = remember { FirebaseAuth.getInstance().currentUser }
    val photoUrl = userFirebase?.photoUrl
    var showCallDialog by remember { mutableStateOf(false) }
    var usuario by remember { mutableStateOf<Usuario?>(null) }
    var reportes by remember { mutableStateOf<List<Reporte>>(emptyList()) }
    var mapaOscuro by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var mostrarIconos by remember { mutableStateOf(true) }
    var soloHoy by remember { mutableStateOf(false) }
    var sosActivo by remember { mutableStateOf(false) }
    var uidActual by remember { mutableStateOf<String?>(null) }

    val prefs = context.getSharedPreferences("ignored_reports", Context.MODE_PRIVATE)
    val idsIgnorados = remember {
        prefs.getStringSet("ids", emptySet())?.toMutableSet() ?: mutableSetOf()
    }

    var mostrarDialogoProximidad by remember { mutableStateOf(false) }
    var reporteCercano by remember { mutableStateOf<Reporte?>(null) }
    val idsNotificados = remember { mutableSetOf<String>() }

    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val camaraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(MapConstants.DEFAULT_LOCATION, 13f)
    }

    // Obtener usuario actual y uid
    LaunchedEffect(Unit) {
        usuario = authRepositorio.obtenerDatosUsuarioActual()
        uidActual = usuario?.uid
    }

    LaunchedEffect(uidActual) {
        if (uidActual != null) {
            locationShareRepositorio.escucharEstado(uidActual!!) { activo ->
                sosActivo = activo
            }
        }
    }

    // Escuchar reportes
    LaunchedEffect(Unit) {
        reporteRepositorio.escucharReportes { lista ->
            reportes = lista.filter { it.estado in listOf("en_revision", "activo", "verificado") }
        }
    }

    // Solicitar permisos de ubicación
    LaunchedEffect(Unit) {
        if (!locationPermissionsState.allPermissionsGranted) {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }

    // Bucle de notificaciones por proximidad (cada 30 segundos) - sin cambios
    LaunchedEffect(Unit) {
        while (true) {
            if (locationPermissionsState.allPermissionsGranted) {
                val ubicacionActual = locationRepositorio.obtenerUbicacionActual()
                if (ubicacionActual != null) {
                    val ahora = System.currentTimeMillis()
                    val unDiaEnMillis = 24 * 60 * 60 * 1000L

                    // Modal si hay incidente a menos de 50 metros
                    val cercano = reportes.find { repo ->
                        val fechaMs = repo.fecha.toDate().time
                        repo.id !in idsIgnorados &&
                                repo.usuarioId != uidActual &&
                                (ahora - fechaMs) <= unDiaEnMillis &&
                                DistanceUtils.calcularDistanciaMetros(
                                    ubicacionActual.latitude, ubicacionActual.longitude,
                                    repo.latitud, repo.longitud
                                ) < 50.0
                    }
                    if (cercano != null && !mostrarDialogoProximidad) {
                        reporteCercano = cercano
                        mostrarDialogoProximidad = true
                        idsIgnorados.add(cercano.id)
                        prefs.edit().putStringSet("ids", idsIgnorados.toSet()).apply()
                    }

                    // Notificaciones para reportes dentro de 2km
                    reportes.filter { repo ->
                        val fechaMs = repo.fecha.toDate().time
                        repo.id !in idsNotificados &&
                                (ahora - fechaMs) <= unDiaEnMillis &&
                                DistanceUtils.calcularDistanciaMetros(
                                    ubicacionActual.latitude, ubicacionActual.longitude,
                                    repo.latitud, repo.longitud
                                ) < 2000.0
                    }.forEach { repo ->
                        idsNotificados.add(repo.id)
                        NotificationHelper.mostrarNotificacion(
                            context,
                            "Incidente detectado: ${repo.categoria.uppercase()}",
                            repo.descripcion.ifEmpty { "Nuevo reporte en tu zona" },
                            repo.id
                        )
                    }
                }
            }
            delay(30000)
        }
    }

    // Bucle de publicación de ubicación SOLO si SOS está activo (cada 3 segundos)
    LaunchedEffect(sosActivo) {
        if (sosActivo && uidActual != null) {
            while (sosActivo) {
                val loc = locationRepositorio.obtenerUbicacionActual()
                if (loc != null) {
                    locationShareRepositorio.actualizarUbicacion(
                        uidActual!!,
                        loc.latitude,
                        loc.longitude
                    )
                }
                delay(3000)
            }
        }
    }

    // Centrar cámara en ubicación actual al iniciar
    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            val ubicacion = locationRepositorio.obtenerUbicacionActual()
            if (ubicacion != null) {
                userLocation = LatLng(ubicacion.latitude, ubicacion.longitude)
                camaraState.animate(CameraUpdateFactory.newLatLngZoom(userLocation!!, 15f))
            }
        }
    }

    suspend fun volverAMiUbicacion() {
        if (locationPermissionsState.allPermissionsGranted) {
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
                camaraState.animate(CameraUpdateFactory.newLatLngZoom(MapConstants.DEFAULT_LOCATION, 13f))
            }
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun activarSos() {
        val usuarioActual = authRepositorio.obtenerDatosUsuarioActual()
        val ubicacionActual = locationRepositorio.obtenerUbicacionActual()
        if (usuarioActual != null && ubicacionActual != null) {
            // Actualizar ubicación en la UI
            userLocation = LatLng(ubicacionActual.latitude, ubicacionActual.longitude)

            // Crear reporte SOS
            val nuevoSOS = Reporte(
                usuarioId = usuarioActual.uid,
                usuarioNombre = usuarioActual.nombre,
                categoria = "sos",
                descripcion = "ALERTA SOS",
                latitud = ubicacionActual.latitude,
                longitud = ubicacionActual.longitude,
                estado = "verificado"
            )
            reporteRepositorio.enviarReporte(nuevoSOS)
            Toast.makeText(context, "🚨 SOS ENVIADO. Compartiendo ubicación por 5 minutos.", Toast.LENGTH_LONG).show()

            // Iniciar publicación de ubicación en Firestore (con expiración)
            locationShareRepositorio.iniciarSos(
                usuarioActual.uid,
                ubicacionActual.latitude,
                ubicacionActual.longitude,
                300000L
            )
            // No establecer sosActivo = true aquí; lo hará el listener
        } else {
            Toast.makeText(context, "No se pudo obtener ubicación. Activa el GPS.", Toast.LENGTH_SHORT).show()
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
                        navController.navigate("historial_personal")
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
                    locationGranted = locationPermissionsState.allPermissionsGranted,
                    mapaOscuro = mapaOscuro,
                    esPolicia = false,
                    marcadoresVisibles = mostrarIconos,
                    navController = navController,
                    userLocation = userLocation,
                    usuarioIdActual = uidActual,
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

                BotonSOS(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 100.dp, end = 16.dp),
                    isActive = sosActivo,
                    onSosActivado = { activarSos() },
                    onLongPressComplete = { showCallDialog = true }
                )
                // Diálogo de llamada al 105 (aparece tras mantener presionado SOS 3 segundos)
                if (showCallDialog) {
                    AlertDialog(
                        onDismissRequest = { showCallDialog = false },
                        icon = { Icon(Icons.Default.LocalPolice, null, tint = Color(0xFFE04F5F), modifier = Modifier.size(36.dp)) },
                        title = { Text("¿Llamar a emergencias?", fontWeight = FontWeight.Bold) },
                        text = { Text("Se marcará el número 105 (Policía Nacional del Perú). ¿Confirmas la llamada?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showCallDialog = false
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:105"))
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE04F5F))
                            ) {
                                Text("Llamar ahora", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showCallDialog = false }) { Text("Cancelar") }
                        }
                    )
                }


                // Modal de incidente activo a 50 metros
                if (mostrarDialogoProximidad && reporteCercano != null) {
                    AlertDialog(
                        onDismissRequest = { mostrarDialogoProximidad = false },
                        icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE04F5F)) },
                        title = { Text("Incidente activo cerca", fontWeight = FontWeight.Bold) },
                        text = {
                            Text("Hay un reporte de '${reporteCercano?.categoria?.uppercase()}' a menos de 50 metros de tu ubicación. ¿Deseas verlo?")
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val id = reporteCercano?.id
                                    mostrarDialogoProximidad = false
                                    if (id != null) navController.navigate("report_detail/$id")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colores.primary)
                            ) {
                                Text("Ver ahora")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                mostrarDialogoProximidad = false
                            }) {
                                Text("Ignorar", color = colores.primary)
                            }
                        }
                    )
                }
            }
        }
    }
}