package com.example.proyectofinal.components.screens

import android.net.Uri
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
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.proyectofinal.components.navigation.BottomNavigationBar
import com.example.proyectofinal.modelos.Reporte
import com.example.proyectofinal.repositorios.AutenticacionRepositorio
import com.example.proyectofinal.repositorios.LocationRepositorio
import com.example.proyectofinal.repositorios.ReporteRepositorio
import com.example.proyectofinal.repositorios.StorageRepositorio
import com.example.proyectofinal.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

data class CategoryItem(val name: String, val icon: ImageVector, val color: Color)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ReportScreen(navController: NavController) {
    val context = LocalContext.current
    val colores = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val reportRepositorio = remember { ReporteRepositorio() }
    val authRepositorio = remember { AutenticacionRepositorio() }
    val locationRepositorio = remember { LocationRepositorio(context) }
    val storageRepositorio = remember { StorageRepositorio() }

    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )
    
    var descripcion by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf<String?>(null) }
    var enviando by remember { mutableStateOf(false) }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imagenUri = uri
    }

    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Nuevo Reporte", color = colores.primary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = colores.primary)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(colores.onSurface.copy(alpha = 0.1f))) {
                            Icon(Icons.Default.Person, null, tint = colores.primary, modifier = Modifier.padding(4.dp))
                        }
                    }
                }
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
                onClick = { galleryLauncher.launch("image/*") }
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Categoría del incidente", fontWeight = FontWeight.Bold)
                
                val categorias = listOf(
                    CategoryItem("Robo", Icons.Default.GppBad, CategoryRobo),
                    CategoryItem("Vandalismo", Icons.Default.Edit, CategoryVandalismo),
                    CategoryItem("Pelea", Icons.Default.Groups, CategoryPelea),
                    CategoryItem("Drogas", Icons.Default.MedicalServices, CategoryDrogas),
                    CategoryItem("Acoso", Icons.Default.RecordVoiceOver, CategoryAcoso),
                    CategoryItem("Infraestructura", Icons.Default.Build, CategoryInfraestructura)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in categorias.indices step 2) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TarjetaCategoria(
                                item = categorias[i],
                                estaSeleccionado = categoriaSeleccionada == categorias[i].name,
                                modifier = Modifier.weight(1f),
                                onClick = { categoriaSeleccionada = categorias[i].name }
                            )
                            if (i + 1 < categorias.size) {
                                TarjetaCategoria(
                                    item = categorias[i+1],
                                    estaSeleccionado = categoriaSeleccionada == categorias[i+1].name,
                                    modifier = Modifier.weight(1f),
                                    onClick = { categoriaSeleccionada = categorias[i+1].name }
                                )
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción (Opcional)") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !enviando
            )

            Button(
                onClick = {
                    if (categoriaSeleccionada != null) {
                        enviando = true
                        scope.launch {
                            val usuario = authRepositorio.obtenerDatosUsuarioActual()
                            val ubicacion = if (locationPermissionState.status.isGranted) {
                                locationRepositorio.obtenerUbicacionActual()
                            } else null

                            var fotoUrl: String? = null
                            if (imagenUri != null) {
                                val uploadResult = storageRepositorio.subirImagen(imagenUri!!, "reportes")
                                if (uploadResult.isSuccess) {
                                    fotoUrl = uploadResult.getOrNull()
                                }
                            }

                            val nuevoReporte = Reporte(
                                usuarioId = usuario?.id ?: "",
                                usuarioNombre = "${usuario?.nombres} ${usuario?.apellidos}",
                                categoria = categoriaSeleccionada!!,
                                descripcion = descripcion,
                                latitud = ubicacion?.latitude ?: 0.0,
                                longitud = ubicacion?.longitude ?: 0.0,
                                fotoUrl = fotoUrl
                            )
                            val resultado = reportRepositorio.enviarReporte(nuevoReporte)
                            enviando = false
                            if (resultado.isSuccess) {
                                navController.popBackStack()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = categoriaSeleccionada != null && !enviando
            ) {
                if (enviando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(colores.primaryContainer.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .border(1.dp, colores.outlineVariant, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (imagenUri != null) {
            Image(
                painter = rememberAsyncImagePainter(imagenUri),
                contentDescription = "Imagen seleccionada",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.PhotoCamera, null, tint = colores.primary, modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(12.dp))
                Text("Toca para tomar foto o adjuntar", color = colores.onSurfaceVariant, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun TarjetaCategoria(item: CategoryItem, estaSeleccionado: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val colores = MaterialTheme.colorScheme
    Card(
        modifier = modifier.height(80.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (estaSeleccionado) item.color.copy(alpha = 0.1f) else colores.surface
        ),
        border = BorderStroke(1.dp, if (estaSeleccionado) item.color else colores.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(item.icon, null, tint = item.color, modifier = Modifier.size(20.dp))
            Text(item.name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}
