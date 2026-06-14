package com.example.proyectofinal.components.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import android.provider.Settings
import java.io.File
import com.example.proyectofinal.components.auth.PrimaryInputField
import com.example.proyectofinal.servicios.GeocodingService
import com.google.android.gms.maps.model.LatLng as MapsLatLng
import com.example.proyectofinal.components.navigation.BottomNavigationBar
import com.example.proyectofinal.modelos.Reporte
import com.example.proyectofinal.repositorios.AutenticacionRepositorio
import com.example.proyectofinal.repositorios.LocationRepositorio
import com.example.proyectofinal.repositorios.ReporteRepositorio
import com.example.proyectofinal.repositorios.StorageRepositorio
import com.example.proyectofinal.servicios.FCMHelper
import com.example.proyectofinal.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

data class CategoryItem(val name: String, val icon: ImageVector, val color: Color)

// Comprime y devuelve ByteArray directamente — sin FileProvider, sin paso intermedio a disco
suspend fun comprimirImagen(context: Context, uri: Uri): ByteArray? = withContext(Dispatchers.IO) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
        val original = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        val maxPx = 1024
        val ratio = minOf(maxPx.toFloat() / original.width, maxPx.toFloat() / original.height)
        val scaled = if (ratio < 1f) {
            Bitmap.createScaledBitmap(
                original,
                (original.width * ratio).toInt(),
                (original.height * ratio).toInt(),
                true
            )
        } else original

        val baos = java.io.ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        if (scaled !== original) scaled.recycle()
        original.recycle()

        baos.toByteArray()
    } catch (_: Exception) {
        null
    }
}

fun crearArchivoFotoTemporal(context: Context): Uri {
    val carpeta = File(context.cacheDir, "camera_photos").apply { mkdirs() }
    val archivo = File(carpeta, "foto_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ReportScreen(navController: NavController) {
    val authRepositorio = AutenticacionRepositorio()
    val nav = navController
    var autorizado by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        val usuario = authRepositorio.obtenerDatosUsuarioActual()

        if (usuario?.rol == "policia") {
            nav.popBackStack()
            autorizado = false
        } else {
            autorizado = true
        }
    }

    if (autorizado != true) return

    val context = LocalContext.current
    val colores = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val reportRepositorio = remember { ReporteRepositorio() }
    val authRepositorioRemember = remember { AutenticacionRepositorio() }
    val locationRepositorio = remember { LocationRepositorio(context) }
    val storageRepositorio = remember { StorageRepositorio() }

    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    var descripcion by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf<String?>(null) }
    var categoriaError by remember { mutableStateOf<String?>(null) }
    var enviando by remember { mutableStateOf(false) }
    var mostrarModalUbicacion by remember { mutableStateOf(false) }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    var ubicacionCacheada by remember { mutableStateOf<android.location.Location?>(null) }
    var usuarioCacheado by remember { mutableStateOf<com.example.proyectofinal.modelos.Usuario?>(null) }

    var uriParaCamara by remember {
        mutableStateOf(crearArchivoFotoTemporal(context))
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { fotoTomada ->
        if (fotoTomada) {
            imagenUri = uriParaCamara
            uriParaCamara = crearArchivoFotoTemporal(context)
        }
    }

    // ESTADO REAL DEL GPS SENSOR DEL EQUIPO
    var gpsActivo by remember { mutableStateOf(false) }

    // Monitoreo constante en segundo plano para saber si el GPS físico está encendido
    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        while(true) {
            gpsActivo = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            delay(1000)
        }
    }

    // Pre-caché de ubicación y datos de usuario en cuanto el permiso y GPS están listos
    LaunchedEffect(locationPermissionState.status.isGranted, gpsActivo) {
        if (locationPermissionState.status.isGranted && gpsActivo) {
            ubicacionCacheada = locationRepositorio.obtenerUbicacionActual()
        }
    }

    LaunchedEffect(Unit) {
        usuarioCacheado = authRepositorioRemember.obtenerDatosUsuarioActual()
    }

    val ubicacionCompletamenteValida = locationPermissionState.status.isGranted && gpsActivo

    // Mostrar modal de activación al entrar si no hay ubicación válida
    LaunchedEffect(gpsActivo, locationPermissionState.status.isGranted) {
        if (!ubicacionCompletamenteValida) {
            mostrarModalUbicacion = true
        }
    }

    // Modal de activación de ubicación
    if (mostrarModalUbicacion && !ubicacionCompletamenteValida) {
        AlertDialog(
            onDismissRequest = {
                mostrarModalUbicacion = false
                navController.popBackStack()
            },
            icon = { Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp)) },
            title = { Text("Ubicación requerida", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    if (!locationPermissionState.status.isGranted)
                        "Para subir un reporte necesitamos acceso a tu ubicación. ¿Conceder permiso ahora?"
                    else
                        "El GPS de tu celular está apagado. Para subir un reporte necesitamos tu ubicación. ¿Activar ahora?",
                    color = Color(0xFF1E3A8A)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarModalUbicacion = false
                        if (!locationPermissionState.status.isGranted) {
                            locationPermissionState.launchPermissionRequest()
                        } else {
                            context.startActivity(android.content.Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Activar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarModalUbicacion = false
                    navController.popBackStack()
                }) { Text("Cancelar", color = MaterialTheme.colorScheme.primary) }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Nuevo reporte",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E3A8A))
            )
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SeccionCapturaFoto(
                imagenUri = imagenUri,
                onClick = {
                    if (cameraPermissionState.status.isGranted) {
                        cameraLauncher.launch(uriParaCamara)
                    } else {
                        cameraPermissionState.launchPermissionRequest()
                    }
                }
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Categoría del incidente",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A),
                        fontSize = 15.sp
                    )
                    if (categoriaSeleccionada != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                categoriaSeleccionada!!,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                val categorias = listOf(
                    CategoryItem("Robo", Icons.Default.GppBad, CategoryRobo),
                    CategoryItem("Vandalismo", Icons.Default.Edit, CategoryVandalismo),
                    CategoryItem("Pelea", Icons.Default.Groups, CategoryPelea),
                    CategoryItem("Drogas", Icons.Default.MedicalServices, CategoryDrogas),
                    CategoryItem("Acoso", Icons.Default.RecordVoiceOver, CategoryAcoso),
                    CategoryItem("Infraestructura", Icons.Default.Build, CategoryInfraestructura)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    categorias.chunked(3).forEach { fila ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            fila.forEach { cat ->
                                TarjetaCategoria(
                                    item = cat,
                                    estaSeleccionado = categoriaSeleccionada == cat.name,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        categoriaSeleccionada = cat.name
                                        categoriaError = null
                                    }
                                )
                            }
                            repeat(3 - fila.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                }

                if (categoriaError != null) {
                    Text(
                        categoriaError!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción (Opcional)", color = Color(0xFF3B82F6)) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !enviando,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF1E293B))
            )

            // UNICA ALERTA CRITICA VISUAL SI ALGO FALLA CON LA UBICACIÓN
            if (!ubicacionCompletamenteValida) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFEBAA))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFF856404))
                            Text(
                                text = "Ubicación obligatoria",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF856404),
                                fontSize = 15.sp
                            )
                        }

                        // Mensaje dinámico dependiendo de qué es exactamente lo que falta activar
                        val mensajeAlerta = if (!locationPermissionState.status.isGranted) {
                            "No puedes realizar reportes sin aceptar los permisos de ubicación de la aplicación."
                        } else {
                            "El GPS de tu celular está apagado. Por favor, actívalo en la barra de notificaciones para poder ubicar tu reporte."
                        }

                        Text(text = mensajeAlerta, color = Color(0xFF856404), fontSize = 13.sp)

                        // Si falta el permiso, mostramos explícitamente el botón para concederlo
                        if (!locationPermissionState.status.isGranted) {
                            Button(
                                onClick = { locationPermissionState.launchPermissionRequest() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF856404)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Conceder permiso", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // BOTÓN DE ENVIAR REPORTES
            Button(
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(12.dp),
                onClick = {
                    if (enviando) return@Button
                    // Validación: categoría siempre requerida + (descripción o imagen)
                    when {
                        categoriaSeleccionada == null ->
                            categoriaError = "Selecciona una categoría del incidente"
                        descripcion.isBlank() && imagenUri == null ->
                            categoriaError = "Agrega una descripción o una foto de evidencia"
                        ubicacionCompletamenteValida -> {
                            categoriaError = null
                            enviando = true
                            scope.launch {
                                try {
                                    val usuario = usuarioCacheado
                                        ?: authRepositorioRemember.obtenerDatosUsuarioActual()
                                    val ubicacion = ubicacionCacheada
                                        ?: locationRepositorio.obtenerUbicacionActual()

                                    // Geocodificación + compresión/upload en PARALELO
                                    val direccionDeferred = async {
                                        runCatching {
                                            if (ubicacion != null)
                                                GeocodingService.getAddressFromLatLng(
                                                    context,
                                                    MapsLatLng(ubicacion.latitude, ubicacion.longitude)
                                                )
                                            else null
                                        }.getOrNull()
                                    }
                                    val fotoDeferred = async {
                                        runCatching {
                                            val uri = imagenUri
                                            if (uri != null) {
                                                val bytes = comprimirImagen(context, uri)
                                                if (bytes != null)
                                                    storageRepositorio.subirImagen(bytes, "reportes").getOrNull()
                                                else null
                                            } else null
                                        }.getOrNull()
                                    }

                                    val direccion = direccionDeferred.await()
                                    val fotoUrl = fotoDeferred.await()

                                    val nuevoReporte = Reporte(
                                        usuarioId = usuario?.uid ?: "",
                                        usuarioNombre = usuario?.nombre ?: "Usuario",
                                        categoria = categoriaSeleccionada!!,
                                        descripcion = descripcion,
                                        latitud = ubicacion?.latitude ?: 0.0,
                                        longitud = ubicacion?.longitude ?: 0.0,
                                        direccion = direccion ?: "Dirección no disponible",
                                        fotoUrl = fotoUrl,
                                        fecha = com.google.firebase.Timestamp.now()
                                    )
                                    val resultado = reportRepositorio.enviarReporte(nuevoReporte)
                                    if (resultado.isSuccess) {
                                        FCMHelper.enviarNotificacionGlobal(
                                            context,
                                            "Nuevo Reporte: ${nuevoReporte.categoria.uppercase()}",
                                            "Se ha reportado un incidente: ${nuevoReporte.descripcion.take(50)}"
                                        )
                                        Toast.makeText(context, "Reporte enviado con éxito", Toast.LENGTH_SHORT).show()
                                        navController.popBackStack()
                                    } else {
                                        Toast.makeText(context, "Error al enviar el reporte", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error inesperado: ${e.message}", Toast.LENGTH_LONG).show()
                                } finally {
                                    enviando = false
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = ubicacionCompletamenteValida,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            ) {
                if (enviando) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Enviando...", fontWeight = FontWeight.Bold, color = Color.White)
                } else {
                    Text("Enviar reporte", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SeccionCapturaFoto(imagenUri: Uri?, onClick: () -> Unit) {
    val colores = MaterialTheme.colorScheme
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (imagenUri != null) colores.primary.copy(alpha = 0.4f) else Color(0xFFE2E8F0)),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (imagenUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imagenUri),
                    contentDescription = "Imagen seleccionada",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Surface(
                        modifier = Modifier.padding(12.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.92f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Edit, null, tint = colores.primary, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Cambiar foto", color = colores.primary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(colores.primary.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PhotoCamera, null, tint = colores.primary, modifier = Modifier.size(34.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Agregar foto de evidencia",
                        fontWeight = FontWeight.SemiBold,
                        color = colores.primary,
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Opcional · Toca para abrir la cámara",
                        color = Color(0xFF3B82F6),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TarjetaCategoria(item: CategoryItem, estaSeleccionado: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(64.dp).clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (estaSeleccionado) item.color.copy(alpha = 0.1f) else Color.White
        ),
        border = BorderStroke(
            width = if (estaSeleccionado) 1.5.dp else 1.dp,
            color = if (estaSeleccionado) item.color else Color(0xFFE2E8F0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(item.icon, null, tint = item.color, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(4.dp))
            Text(
                item.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (estaSeleccionado) item.color else Color(0xFF1E3A8A),
                maxLines = 1
            )
        }
    }
}