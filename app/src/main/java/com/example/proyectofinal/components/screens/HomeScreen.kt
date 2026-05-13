package com.example.proyectofinal.components.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.proyectofinal.ui.theme.GreenPrimary
import com.example.proyectofinal.ui.theme.OrangeAlert
import com.example.proyectofinal.ui.theme.RedEmergency
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController
) {
    val colors = MaterialTheme.colorScheme
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                modifier = Modifier.width(300.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
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
                        Text(
                            "Mariana Rodríguez",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            "mariana.rodriguez@email.com",
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
                        unselectedTextColor = Color.DarkGray
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
                        unselectedTextColor = Color.DarkGray
                    )
                )

                Spacer(modifier = Modifier.weight(1f))
                
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null) },
                    label = { Text("Cerrar Sesión") },
                    selected = false,
                    onClick = {
                        navController.navigate("login") { popUpTo(0) }
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
                                    .background(Color.Gray)
                            ) {
                                Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.padding(4.dp))
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
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
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(colors.primary.copy(alpha = 0.1f), colors.primary.copy(alpha = 0.2f))
                        )
                    )
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
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(0.65f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Mapa de Seguridad", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colors.primary))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Estado: Seguro", fontSize = 14.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { 0.7f },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                color = colors.primary,
                trackColor = colors.outlineVariant
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
    val colors = MaterialTheme.colorScheme
    Surface(
        color = if (isSelected) colors.primary else colors.surface,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = if (isSelected) Color.White else Color.Red, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(text, color = if (isSelected) Color.White else Color.Black, fontSize = 12.sp)
        }
    }
}

@Composable
fun MapActionButton(icon: ImageVector) {
    Surface(
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        IconButton(onClick = { }) {
            Icon(icon, contentDescription = null, tint = Color.DarkGray)
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val colors = MaterialTheme.colorScheme
    NavigationBar(containerColor = colors.surface) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.Map, "Map") },
            label = { Text("Mapa") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("report") },
            icon = { Icon(Icons.Default.AddCircle, "Report") },
            label = { Text("Reportes") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.Notifications, "Alerts") },
            label = { Text("Alertas") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("profile") },
            icon = { Icon(Icons.Default.Person, "Profile") },
            label = { Text("Perfil") }
        )
    }
}
