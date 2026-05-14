package com.example.proyectofinal.components.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyectofinal.components.navigation.BottomNavigationBar
import com.example.proyectofinal.modelos.Usuario
import com.example.proyectofinal.repositorios.AutenticacionRepositorio
import com.example.proyectofinal.ui.theme.GreenPrimary
import com.example.proyectofinal.ui.theme.OrangeAlert
import com.example.proyectofinal.ui.theme.RedEmergency
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController
) {
    // SOPORTE MODO OSCURO: Usamos el esquema de colores del tema actual
    val colores = MaterialTheme.colorScheme
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val repositorio = remember { AutenticacionRepositorio() }
    
    var usuario by remember { mutableStateOf<Usuario?>(null) }

    LaunchedEffect(Unit) {
        usuario = repositorio.obtenerDatosUsuarioActual()
    }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                // SOPORTE MODO OSCURO: El fondo del menú lateral se adapta al tema
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
                        // MOSTRAR DATOS: Nombre del usuario sin texto de carga
                        Text(
                            text = if (usuario != null) "${usuario?.nombres} ${usuario?.apellidos}" else "Usuario",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = usuario?.correo ?: "",
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
                        // SOPORTE MODO OSCURO: El texto de los items se adapta
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
                        repositorio.cerrarSesion()
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
                        Text(
                            "SafetyConnect",
                            color = GreenPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { 
                            scope.launch { drawerState.open() }
                        }) {
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
                    // SOPORTE MODO OSCURO: Fondo de la barra adaptable
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = colores.surface)
                )
            },
            bottomBar = {
                BottomNavigationBar(navController = navController)
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(colores.background) // SOPORTE MODO OSCURO: Fondo general
            ) {
                MapContent()

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
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = RedEmergency),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(80.dp)
                            .padding(bottom = 8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("SOS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = OrangeAlert)
                    ) {
                        Box(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Alerta Activa", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
                
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MapActionButton(Icons.Default.Layers)
                    MapActionButton(Icons.Default.MyLocation)
                    MapActionButton(Icons.Default.NearMe)
                }
            }
        }
    }
}

@Composable
fun MapContent() {
    Box(modifier = Modifier.fillMaxSize())
}

@Composable
fun SecurityStatusCard() {
    val colores = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(0.65f),
        shape = RoundedCornerShape(24.dp),
        // SOPORTE MODO OSCURO: Superficie de tarjeta dinámica
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
        // SOPORTE MODO OSCURO: Color de chip adaptable
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

@Composable
fun MapActionButton(icon: ImageVector) {
    val colores = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.size(44.dp),
        shape = RoundedCornerShape(12.dp),
        // SOPORTE MODO OSCURO: Botones flotantes del mapa adaptables
        color = colores.surface,
        shadowElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, colores.outlineVariant)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = colores.primary)
        }
    }
}
