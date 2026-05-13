package com.example.proyectofinal.components.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyectofinal.ui.theme.*

data class CategoryItem(val name: String, val icon: ImageVector, val color: Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(navController: NavController) {
    val colors = MaterialTheme.colorScheme
    var description by remember { mutableStateOf("") }
    var isAnonymous by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Nuevo Reporte",
                        color = colors.primary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.onSurface.copy(alpha = 0.8f))
                        ) {
                            Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.padding(4.dp))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface)
            )
        },
        bottomBar = {
            BottomNavigationBarReport(navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            PhotoPickerSection()

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Categoría del incidente",
                    fontWeight = FontWeight.Bold,
                    color = colors.onBackground.copy(alpha = 0.8f)
                )
                
                val categories = listOf(
                    CategoryItem("Robo", Icons.Default.BugReport, CategoryRobo),
                    CategoryItem("Vandalismo", Icons.Default.Edit, CategoryVandalismo),
                    CategoryItem("Pelea", Icons.Default.Groups, CategoryPelea),
                    CategoryItem("Drogas", Icons.Default.MedicalInformation, CategoryDrogas),
                    CategoryItem("Acoso", Icons.Default.RecordVoiceOver, CategoryAcoso),
                    CategoryItem("Infraestructura", Icons.Default.Build, CategoryInfraestructura)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in categories.indices step 2) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CategoryCard(
                                item = categories[i],
                                isSelected = selectedCategory == categories[i].name,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedCategory = categories[i].name }
                            )
                            if (i + 1 < categories.size) {
                                CategoryCard(
                                    item = categories[i+1],
                                    isSelected = selectedCategory == categories[i+1].name,
                                    modifier = Modifier.weight(1f),
                                    onClick = { selectedCategory = categories[i+1].name }
                                )
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Descripción",
                        fontWeight = FontWeight.Bold,
                        color = colors.onBackground.copy(alpha = 0.8f)
                    )
                    Text("OPCIONAL", fontSize = 10.sp, color = colors.onBackground.copy(alpha = 0.4f))
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Describe qué está ocurriendo...", color = colors.onBackground.copy(alpha = 0.3f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = colors.outlineVariant,
                        focusedBorderColor = colors.primary
                    )
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Ubicación",
                    fontWeight = FontWeight.Bold,
                    color = colors.onBackground.copy(alpha = 0.8f)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.onSurface.copy(alpha = 0.9f))
                ) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(12.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = colors.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Ubicación actual detectada: Av. Refo...", fontSize = 12.sp)
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = colors.onSurfaceVariant)
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Reporte anónimo", fontWeight = FontWeight.Bold)
                        Text("Tu identidad no será revelada", fontSize = 11.sp, color = colors.onSurfaceVariant)
                    }
                    Switch(
                        checked = isAnonymous,
                        onCheckedChange = { isAnonymous = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.onPrimary,
                            checkedTrackColor = colors.primary
                        )
                    )
                }
            }

            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Enviar reporte", color = colors.onPrimary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = colors.onPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PhotoPickerSection() {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(colors.primaryContainer.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape,
                color = colors.surface,
                shadowElevation = 2.dp
            ) {
                Icon(
                    Icons.Default.PhotoCamera,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = colors.primary
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Toca para tomar foto o adjuntar",
                color = colors.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun CategoryCard(item: CategoryItem, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) item.color.copy(alpha = 0.1f) else colors.surface
        ),
        border = BorderStroke(1.dp, if (isSelected) item.color else colors.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(item.icon, contentDescription = null, tint = item.color, modifier = Modifier.size(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(item.color))
                Spacer(Modifier.width(6.dp))
                Text(item.name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun BottomNavigationBarReport(navController: NavController) {
    val colors = MaterialTheme.colorScheme
    NavigationBar(
        containerColor = colors.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("home") },
            icon = { Icon(Icons.Default.Map, contentDescription = null) },
            label = { Text("Map") }
        )
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.AddCircle, contentDescription = null) },
            label = { Text("Report") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
            label = { Text("Alerts") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("profile") },
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text("Profile") }
        )
    }
}
