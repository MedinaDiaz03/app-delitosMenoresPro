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
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val authRepo = remember { AutenticacionRepositorio() }
    val reportRepo = remember { ReporteRepositorio() }
    var usuario by remember { mutableStateOf<Usuario?>(null) }
    var totalReportes by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        usuario = authRepo.obtenerDatosUsuarioActual()
        if (usuario != null) {
            totalReportes = reportRepo.obtenerConteoReportesUsuario(usuario!!.uid)
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        authRepo.cerrarSesion()
                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                        Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, "Salir", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E3A8A))
            )
        },
        bottomBar = { BottomNavigationBar(navController, esPolicia = usuario?.rol == "policia") }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            PerfilCabecera(usuario)

            Spacer(modifier = Modifier.height(24.dp))

            if (usuario?.rol != "policia") {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Estadísticas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFF1E293B)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        EstadisticaChip(
                            modifier = Modifier.weight(1f),
                            valor = totalReportes.toString(),
                            etiqueta = "Reportes",
                            icono = Icons.Default.Campaign,
                            color = MaterialTheme.colorScheme.primary
                        )
                        EstadisticaChip(
                            modifier = Modifier.weight(1f),
                            valor = (usuario?.nivelConfianza ?: 0).toString(),
                            etiqueta = "Confianza",
                            icono = Icons.Default.Verified,
                            color = Color(0xFF3B82F6)
                        )
                        EstadisticaChip(
                            modifier = Modifier.weight(1f),
                            valor = nivelDeConfianza(usuario?.nivelConfianza ?: 0),
                            etiqueta = "Nivel",
                            icono = Icons.Default.Star,
                            color = Color(0xFFF59E0B)
                        )
                    }
                    NivelDeConfianzaCard(puntos = usuario?.nivelConfianza ?: 0)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Mi cuenta",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF1E293B)
                )

                if (usuario?.rol != "policia") {
                    OpcionMenu(
                        icono = Icons.Default.History,
                        titulo = "Historial de reportes",
                        subtitulo = "Ver todos tus reportes enviados",
                        onClick = { navController.navigate("historial_personal") }
                    )
                }
                OpcionMenu(
                    icono = Icons.Default.Public,
                    titulo = "Historial Global",
                    subtitulo = "Ver todos los reportes de la zona",
                    onClick = { navController.navigate("historial_global") }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PerfilCabecera(usuario: Usuario?) {
    val userFirebase = remember { FirebaseAuth.getInstance().currentUser }
    val photoUrl = userFirebase?.photoUrl

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF1E3A8A),
                        Color(0xFF2563EB)
                    )
                )
            )
    ) {
        // Decoración sutil
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-40).dp, y = (-60).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.04f))
        )
        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-20).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.04f))
        )

        Column(
            modifier = Modifier.fillMaxWidth().align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (photoUrl != null) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(48.dp), tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val nombre = when {
                usuario != null && usuario.nombre.isNotEmpty() -> usuario.nombre
                userFirebase?.displayName != null -> userFirebase.displayName
                else -> "Usuario"
            }

            Text(
                text = nombre ?: "Cargando...",
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
                Spacer(modifier = Modifier.height(6.dp))
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
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier.size(34.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, null, tint = color, modifier = Modifier.size(17.dp))
            }
            Text(valor, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = color)
            Text(etiqueta, fontSize = 11.sp, color = Color(0xFF64748B))
        }
    }
}

@Composable
fun NivelDeConfianzaCard(puntos: Int) {
    val meta = if (puntos < 10) 10 else 20
    val progreso = (puntos.toFloat() / meta.toFloat()).coerceIn(0f, 1f)
    val nivel = nivelDeConfianza(puntos)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Nivel de Confianza",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1E293B)
                )
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        nivel,
                        color = MaterialTheme.colorScheme.primary,
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
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color(0xFFE2E8F0)
            )
            Spacer(modifier = Modifier.height(6.dp))
            val proximoNivel = if (puntos < 10) "Confiable" else "Seguro"
            Text(
                if (puntos >= 20) "Nivel máximo alcanzado"
                else "$puntos / $meta puntos para llegar a \"$proximoNivel\"",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
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
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
                Text(subtitulo, fontSize = 12.sp, color = Color(0xFF64748B))
            }
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = Color(0xFFCBD5E1),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

fun nivelDeConfianza(puntos: Int): String = when {
    puntos >= 20 -> "Seguro"
    puntos >= 10 -> "Confiable"
    else -> "Nuevo"
}
