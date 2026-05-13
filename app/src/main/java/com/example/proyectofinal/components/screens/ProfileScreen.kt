package com.example.proyectofinal.components.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyectofinal.ui.theme.GreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val colors = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "SafetyConnect",
                            color = GreenPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.NotificationsNone, contentDescription = "Notifications")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            BottomNavigationBarProfile(navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA))
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Profile Header
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(3.dp, GreenPrimary, CircleShape)
                        .background(Color.LightGray)
                ) {
                    // In a real app, use AsyncImage. For now, a placeholder icon.
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        tint = Color.White
                    )
                }
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = GreenPrimary,
                    shadowElevation = 2.dp
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Mariana Rodríguez",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.Black
            )
            Text(
                "Guardián Comunitario • Miembro desde 2022",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Trust Level Card
            TrustLevelCard()

            Spacer(modifier = Modifier.height(24.dp))

            // Action Cards
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Reportes Realizados",
                    value = "142",
                    icon = Icons.Default.Campaign,
                    containerColor = GreenPrimary,
                    onClick = { navController.navigate("historial_repo") }
                )
                StatCard("Ayuda Comunitaria", "56", Icons.Default.Handshake, Color.White, GreenPrimary)
                StatCard("Insignias Ganadas", "12", Icons.Default.EmojiEvents, Color.White, Color(0xFFFDE8E8), Color(0xFFE04F5F))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Emergency Contacts
            EmergencyContactsSection()

            Spacer(modifier = Modifier.height(32.dp))

            // Account Configuration
            AccountConfigSection(navController)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun TrustLevelCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "NIVEL DE CONFIANZA",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { 0.85f },
                    modifier = Modifier.size(120.dp),
                    color = GreenPrimary,
                    strokeWidth = 12.dp,
                    trackColor = Color(0xFFE9ECEF)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("85%", fontWeight = FontWeight.Bold, fontSize = 28.sp)
                    Text("Excelente", fontSize = 12.sp, color = Color.Gray)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Trusted by 240+ neighbors", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color = Color.White,
    iconBgColor: Color = Color(0xFFE6F4F1),
    iconColor: Color = GreenPrimary,
    onClick: () -> Unit = {}
) {
    // If containerColor is GreenPrimary, we swap colors as in the mockup
    val isPrimary = containerColor == GreenPrimary
    val finalContainerColor = if (isPrimary) GreenPrimary else Color.White
    val finalContentColor = if (isPrimary) Color.White else Color.Black
    val finalIconBg = if (isPrimary) Color(0xFF095A41) else iconBgColor
    val finalIconColor = if (isPrimary) Color.White else iconColor

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = finalContainerColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(finalIconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = finalIconColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, color = finalContentColor.copy(alpha = 0.7f))
                Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = finalContentColor)
            }
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = finalContentColor.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun EmergencyContactsSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Contactos de Emergencia", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Editar", color = GreenPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { ContactItem("Roberto (Papá)", "SOS", Color(0xFFFDE8E8), Color(0xFFE04F5F)) }
            item { ContactItem("Elena (Hermana)", "CONFIABLE", Color(0xFFE6F4F1), GreenPrimary) }
            item { 
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun ContactItem(name: String, tag: String, tagBg: Color, tagColor: Color) {
    Card(
        modifier = Modifier.width(140.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            ) {
                Icon(Icons.Default.Person, null, modifier = Modifier.fillMaxSize().padding(8.dp), tint = Color.White)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(name, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = tagBg,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    tag,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = tagColor
                )
            }
        }
    }
}

@Composable
fun AccountConfigSection(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text("Configuración de Cuenta", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                ConfigMenuItem(Icons.Default.Person, "Información del Perfil")
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = Color(0xFFF1F3F5))
                ConfigMenuItem(Icons.Default.Security, "Privacidad y Seguridad")
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = Color(0xFFF1F3F5))
                ConfigMenuItem(Icons.Default.Wifi, "Vinculación de Dispositivo (SafeLink)")
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = Color(0xFFF1F3F5))
                ConfigMenuItem(
                    Icons.AutoMirrored.Filled.Logout, 
                    "Cerrar Sesión", 
                    textColor = Color(0xFFE04F5F),
                    onClick = { navController.navigate("login") { popUpTo(0) } }
                )
            }
        }
    }
}

@Composable
fun ConfigMenuItem(
    icon: ImageVector,
    title: String,
    textColor: Color = Color.Black,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = if(textColor == Color.Black) Color.Gray else textColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, modifier = Modifier.weight(1f), color = textColor, fontSize = 15.sp)
        if (textColor == Color.Black) {
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun BottomNavigationBarProfile(navController: NavController) {
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("home") },
            icon = { Icon(Icons.Default.Map, "Mapa") },
            label = { Text("Mapa") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("report") },
            icon = { Icon(Icons.Default.AddCircle, "Reportar") },
            label = { Text("Reportar") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.Report, "Alertas") },
            label = { Text("Alertas") }
        )
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.Person, "Perfil") },
            label = { Text("Perfil") }
        )
    }
}
