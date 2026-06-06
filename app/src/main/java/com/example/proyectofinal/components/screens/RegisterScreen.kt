package com.example.proyectofinal.components.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyectofinal.components.auth.*
import com.example.proyectofinal.modelos.Usuario
import com.example.proyectofinal.repositorios.AutenticacionRepositorio
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repositorio = remember { AutenticacionRepositorio() }

    var nombreCompleto by remember { mutableStateOf("") }
    // var dni by remember { mutableStateOf("") } // DNI ya no es parte del modelo Usuario
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var aceptoTerminos by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var codigoPolicia by remember { mutableStateOf("") }

    // Función para validar que solo sean letras (permite espacios)
    fun soloLetras(texto: String): Boolean {
        return texto.all { it.isLetter() || it.isWhitespace() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        AuthHeader(
            title = "Crear cuenta",
            subtitle = "Únete a nuestra comunidad y comienza a mejorar la seguridad ciudadana",
            icon = Icons.Default.PersonAdd,
            isHero = false
        )

        AuthCardContainer(
            modifier = Modifier.offset(y = (-20).dp)
        ) {

            Spacer(modifier = Modifier.height(16.dp))

                PrimaryInputField(
                    value = nombreCompleto,
                    onValueChange = { if (soloLetras(it)) nombreCompleto = it },
                    label = "Nombre completo",
                    placeholder = "Ej. Juan Pérez",
                    leadingIcon = Icons.Default.Person,
                    modifier = Modifier.fillMaxWidth()
                )

            Spacer(modifier = Modifier.height(16.dp))

            /*
            // DNI ya no se guarda en el modelo Usuario
            PrimaryInputField(
                value = dni,
                onValueChange = { if (it.length <= 8 && it.all { char -> char.isDigit() }) dni = it },
                label = "DNI",
                placeholder = "Número de documento",
                leadingIcon = Icons.Default.Badge
            )
            */

            Spacer(modifier = Modifier.height(16.dp))

            // Correo (@gmail.com obligatorio)
            PrimaryInputField(
                value = correo,
                onValueChange = { correo = it },
                label = "Correo electrónico",
                placeholder = "nombre@gmail.com",
                leadingIcon = Icons.Default.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryInputField(
                value = codigoPolicia,
                onValueChange = { codigoPolicia = it },
                label = "Código de oficial (opcional)",
                placeholder = "Solo si eres policía",
                leadingIcon = Icons.Default.Security
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryInputField(
                value = password,
                onValueChange = { password = it },
                label = "Contraseña",
                placeholder = "********",
                leadingIcon = Icons.Default.Lock,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryInputField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirmar contraseña",
                placeholder = "********",
                leadingIcon = Icons.Default.Lock,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            TermsCheckbox(
                checked = aceptoTerminos,
                onCheckedChange = { aceptoTerminos = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                PrimaryButton(
                    text = "Crear cuenta",
                    onClick = {
                        // REQUISITO: Todos los campos llenos
                        if (nombreCompleto.isBlank() || 
                            correo.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                            Toast.makeText(context, "Todos los campos tienen que ser llenados", Toast.LENGTH_SHORT).show()
                            return@PrimaryButton
                        }

                        // REQUISITO: Correo @gmail.com
                        if (!correo.lowercase().endsWith("@gmail.com")) {
                            Toast.makeText(context, "Correo no aceptable", Toast.LENGTH_SHORT).show()
                            return@PrimaryButton
                        }

                        if (password != confirmPassword) {
                            Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                            return@PrimaryButton
                        }

                        if (!aceptoTerminos) {
                            Toast.makeText(context, "Debe aceptar los términos", Toast.LENGTH_SHORT).show()
                            return@PrimaryButton
                        }

                        isLoading = true

                        val esCodigoValido = codigoPolicia == "POLICIA123"

                        if (codigoPolicia.isNotBlank() && !esCodigoValido) {
                            Toast.makeText(context, "Código de policía incorrecto", Toast.LENGTH_SHORT).show()
                            isLoading = false
                            return@PrimaryButton
                        }

                        val nuevoUsuario = Usuario(
                            nombre = nombreCompleto,
                            email = correo,
                            rol = if (esCodigoValido) "policia" else "ciudadano",
                            verificado = esCodigoValido
                        )

                        scope.launch {
                            val resultado = repositorio.registrarUsuario(nuevoUsuario, password)
                            isLoading = false
                            if (resultado.isSuccess) {
                                Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                navController.navigate("home") {
                                    popUpTo("register") { inclusive = true }
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                Toast.makeText(context, "Error: ${resultado.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            DividerWithText(text = "o regístrate con")

            Spacer(modifier = Modifier.height(16.dp))

            GoogleButton(onClick = { })
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "¿Ya tienes cuenta? ")
            Text(
                text = "Iniciar sesión",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { navController.popBackStack() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
