package com.example.proyectofinal.components.screens


import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
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
import coil.compose.AsyncImage
import com.example.proyectofinal.helpers.RolHelper
import com.google.firebase.auth.FirebaseAuth

// ─── PANTALLA DE PERFIL ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val authRepo   = remember { AutenticacionRepositorio() }
    val reportRepo = remember { ReporteRepositorio() }
    var usuario        by remember { mutableStateOf<Usuario?>(null) }
    var totalReportes  by remember { mutableIntStateOf(0) }
    val colores = MaterialTheme.colorScheme

    // Carga de datos al entrar
    LaunchedEffect(Unit) {
        usuario = authRepo.obtenerDatosUsuarioActual()
        if (usuario != null) {
            totalReportes = reportRepo.obtenerConteoReportesUsuario(usuario!!.uid)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = colores.onSurface)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        authRepo.cerrarSesion()
                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                        Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, "Salir", tint = Color(0xFFE04F5F))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(colores.background)
        ) {
            // ── CABECERA CON GRADIENTE ──
            PerfilCabecera(usuario)

            Spacer(modifier = Modifier.height(24.dp))

            // ── ESTADÍSTICAS ──
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Estadísticas", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    EstadisticaChip(
                        modifier = Modifier.weight(1f),
                        valor = totalReportes.toString(),
                        etiqueta = "Reportes",
                        icono = Icons.Default.Campaign,
                        color = GreenPrimary
                    )
                    EstadisticaChip(
                        modifier = Modifier.weight(1f),
                        valor = (0).toString(), // TODO: Adaptar si se agregan estos campos al nuevo modelo
                        etiqueta = "Validados",
                        icono = Icons.Default.Verified,
                        color = Color(0xFF3B82F6)
                    )
                    EstadisticaChip(
                        modifier = Modifier.weight(1f),
                        valor = nivelDeConfianza(0),
                        etiqueta = "Nivel",
                        icono = Icons.Default.Star,
                        color = Color(0xFFF59E0B)
                    )
                }

                // Barra de progreso de nivel
                NivelDeConfianzaCard(puntos = 0)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── VERIFICACIÓN OFICIAL ──
            if (usuario?.rol != "policia") {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Validación de Personal", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    var codigoInput by remember { mutableStateOf("") }
                    val rolHelper = remember { RolHelper() }

                    OutlinedTextField(
                        value = codigoInput,
                        onValueChange = { codigoInput = it },
                        label = { Text("Código de Oficial") },
                        placeholder = { Text("Ingresa el código secreto") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        trailingIcon = {
                            IconButton(onClick = { rolHelper.validarCodigoPolicia(context, codigoInput) }) {
                                Icon(Icons.Default.VerifiedUser, null, tint = GreenPrimary)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            focusedLabelColor = GreenPrimary
                        )
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── MENÚ DE OPCIONES ──
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Mi cuenta", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                OpcionMenu(
                    icono = Icons.Default.History,
                    titulo = "Historial de reportes",
                    subtitulo = "Ver todos tus reportes enviados",
                    onClick = { navController.navigate("historial_repo") }
                )
                OpcionMenu(
                    icono = Icons.Default.Public,
                    titulo = "Historial Global 🌍",
                    subtitulo = "Ver todos los reportes de la zona",
                    onClick = { navController.navigate("historial_global") }
                )
                OpcionMenu(
                    icono = Icons.Default.ContactPhone,
                    titulo = "Contactos de emergencia",
                    subtitulo = "Gestionar contactos de confianza",
                    onClick = { /* TODO */ }
                )
                OpcionMenu(
                    icono = Icons.Default.Notifications,
                    titulo = "Notificaciones",
                    subtitulo = "Alertas y avisos de la comunidad",
                    onClick = { /* TODO */ }
                )
                OpcionMenu(
                    icono = Icons.Default.Info,
                    titulo = "Acerca de SafetyConnect",
                    subtitulo = "Versión 1.0.0",
                    onClick = { /* TODO */ }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ─── COMPONENTES ──────────────────────────────────────────────────────────────

@Composable
fun PerfilCabecera(usuario: Usuario?) {
    val userFirebase = remember { FirebaseAuth.getInstance().currentUser }
    val photoUrl = userFirebase?.photoUrl

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(
                Brush.linearGradient(listOf(GreenPrimary, Color(0xFF095A41)))
            )
    ) {
        // Decoración de círculos en el fondo
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-40).dp, y = (-60).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )
        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-20).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )

        // Contenido centrado
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar con soporte para foto de Google
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .border(3.dp, Color.White.copy(alpha = 0.6f), CircleShape),
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
                        modifier = Modifier.size(52.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fallback lógico: Prioriza Firestore, luego Firebase Auth, luego placeholder
            val nombreAMostrar = when {
                usuario != null && usuario.nombre.isNotEmpty() -> usuario.nombre
                userFirebase?.displayName != null -> userFirebase.displayName
                else -> "Usuario"
            }

            Text(
                text = nombreAMostrar ?: "Cargando...",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Text(
                text = usuario?.email ?: userFirebase?.email ?: "",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp
            )
            
            if (usuario?.rol == "policia") {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        "Oficial Verificado",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EstadisticaChip(
    modifier: Modifier = Modifier,
    valor: String,
    etiqueta: String,
    icono: ImageVector,
    color: Color
) {
    val colores = MaterialTheme.colorScheme
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colores.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, null, tint = color, modifier = Modifier.size(18.dp))
            }
            Text(valor, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = color)
            Text(etiqueta, fontSize = 11.sp, color = colores.onSurfaceVariant)
        }
    }
}

@Composable
fun NivelDeConfianzaCard(puntos: Int) {
    val colores = MaterialTheme.colorScheme
    val progreso = (puntos.toFloat() / 20f).coerceIn(0f, 1f)
    val nivel = nivelDeConfianza(puntos)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colores.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Nivel de Confianza", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Surface(
                    color = GreenPrimary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        nivel,
                        color = GreenPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = GreenPrimary,
                trackColor = colores.outlineVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "$puntos / 20 puntos por reportes validados",
                fontSize = 12.sp,
                color = colores.onSurfaceVariant
            )
        }
    }
}

@Composable
fun OpcionMenu(
    icono: ImageVector,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit
) {
    val colores = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colores.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GreenPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(subtitulo, fontSize = 12.sp, color = colores.onSurfaceVariant)
            }
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = colores.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Función simple para calcular el nivel
fun nivelDeConfianza(puntos: Int): String = when {
    puntos >= 50 -> "Experto"
    puntos >= 20 -> "Avanzado"
    puntos >= 10 -> "Regular"
    else         -> "Nuevo"
}