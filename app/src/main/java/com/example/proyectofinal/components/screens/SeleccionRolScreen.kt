package com.example.proyectofinal.components.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyectofinal.repositorios.AutenticacionRepositorio
import kotlinx.coroutines.launch

@Composable
fun SeleccionRolScreen(navController: NavController) {
    val context = LocalContext.current
    val repositorio = remember { AutenticacionRepositorio() }
    val alcanceCorrutina = rememberCoroutineScope()

    var rolSeleccionado by remember { mutableStateOf<String?>(null) }
    var codigo by remember { mutableStateOf("") }
    var codigoError by remember { mutableStateOf<String?>(null) }
    var codigoVisible by remember { mutableStateOf(false) }

    Scaffold(containerColor = Color(0xFFF8FAFC)) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "¿Cómo deseas ingresar?",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF1E293B),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Selecciona tu rol para acceder a la app",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF334155),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ── CARDS DE SELECCIÓN ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RolCard(
                    modifier = Modifier.weight(1f),
                    titulo = "Ciudadano",
                    descripcion = "Reporta y consulta incidentes",
                    icon = Icons.Default.People,
                    seleccionado = rolSeleccionado == "ciudadano",
                    onClick = {
                        rolSeleccionado = "ciudadano"
                        alcanceCorrutina.launch {
                            val resultado = repositorio.actualizarRol("ciudadano")
                            if (resultado.isSuccess) {
                                navController.navigate("home") {
                                    popUpTo("seleccion_rol") { inclusive = true }
                                }
                            } else {
                                Toast.makeText(context, "Error al guardar rol", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )

                RolCard(
                    modifier = Modifier.weight(1f),
                    titulo = "Policía",
                    descripcion = "Acceso con código oficial",
                    icon = Icons.Default.Badge,
                    seleccionado = rolSeleccionado == "policia",
                    onClick = {
                        rolSeleccionado = if (rolSeleccionado == "policia") null else "policia"
                        codigoError = null
                        codigo = ""
                    }
                )
            }

            // ── CÓDIGO DE POLICÍA (aparece con animación) ──
            AnimatedVisibility(
                visible = rolSeleccionado == "policia",
                enter = fadeIn(tween(280)) + expandVertically(tween(300, easing = EaseOutQuart)),
                exit = fadeOut(tween(200)) + shrinkVertically(tween(250))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = codigo,
                        onValueChange = { codigo = it; codigoError = null },
                        label = { Text("Código de verificación") },
                        placeholder = { Text("Ingresa tu código oficial", color = Color(0xFF94A3B8)) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, null, tint = Color(0xFF64748B))
                        },
                        isError = codigoError != null,
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = Color(0xFF64748B),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            errorContainerColor = Color(0xFFFFF5F5),
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    if (codigoError != null) {
                        Text(
                            text = codigoError!!,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (codigo.isBlank()) {
                                codigoError = "Ingresa el código de verificación"
                                return@Button
                            }
                            if (codigo != "POLICIA123") {
                                codigoError = "Código incorrecto"
                                Toast.makeText(context, "Código incorrecto", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            alcanceCorrutina.launch {
                                val resultado = repositorio.actualizarRol("policia", verificado = true)
                                if (resultado.isSuccess) {
                                    Toast.makeText(context, "Ahora eres policía ✅", Toast.LENGTH_SHORT).show()
                                    navController.navigate("home") {
                                        popUpTo("seleccion_rol") { inclusive = true }
                                    }
                                } else {
                                    Toast.makeText(context, "Error al validar código", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Icon(Icons.Default.Badge, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Ingresar como policía", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun RolCard(
    modifier: Modifier = Modifier,
    titulo: String,
    descripcion: String,
    icon: ImageVector,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (seleccionado) MaterialTheme.colorScheme.primary else Color(0xFFE2E8F0),
        animationSpec = tween(200),
        label = "border"
    )
    val containerColor by animateColorAsState(
        targetValue = if (seleccionado) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f) else Color.White,
        animationSpec = tween(200),
        label = "container"
    )

    Card(
        onClick = onClick,
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(
            width = if (seleccionado) 2.dp else 1.dp,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = if (seleccionado)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else
                        Color(0xFFF1F5F9)
                ) {}
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (seleccionado) MaterialTheme.colorScheme.primary else Color(0xFF64748B),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = titulo,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = if (seleccionado) MaterialTheme.colorScheme.primary else Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = descripcion,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )
        }
    }
}
