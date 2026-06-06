package com.example.proyectofinal.components.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyectofinal.components.auth.*
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.example.proyectofinal.repositorios.AutenticacionRepositorio
import kotlinx.coroutines.launch
import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*

@Composable
fun LoginScreen(navController: NavController) {
    val contexto = LocalContext.current
    val alcanceCorrutina = rememberCoroutineScope()
    val repositorio = remember { AutenticacionRepositorio() }

    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var estaCargando by remember { mutableStateOf(false) }

    // --- CONFIGURACIÓN GOOGLE SIGN-IN ---
    val auth = FirebaseAuth.getInstance()
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("738190982265-r5q3tl92qurcgqn362ds9acghn1pist3.apps.googleusercontent.com")
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(contexto, gso) }

    fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                estaCargando = false
                if (task.isSuccessful) {
                    repositorio.guardarUsuarioEnFirestore()
                    Toast.makeText(contexto, "Bienvenido: ${auth.currentUser?.displayName}", Toast.LENGTH_SHORT).show()
                    navController.navigate("seleccion_rol") {
                        popUpTo("login") { inclusive = true }
                    }
                } else {
                    Toast.makeText(contexto, "Error en Firebase: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { firebaseAuthWithGoogle(it) }
            } catch (e: ApiException) {
                estaCargando = false
                Toast.makeText(contexto, "Error de Google: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        } else {
            estaCargando = false
        }
    }
    // ------------------------------------

    // Verificar si el usuario ya inició sesión para saltar el login
    LaunchedEffect(Unit) {
        if (repositorio.obtenerUsuarioActual() != null) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {

        AuthHeader(
            title = "Iniciar sesión",
            subtitle = "Bienvenido",
            icon = Icons.Default.Lock,
            isHero = true
        )

        AuthCardContainer(modifier = Modifier.offset(y = (-28).dp)) {

            PrimaryInputField(
                value = correo,
                onValueChange = { correo = it },
                label = "Correo electrónico",
                placeholder = "usuario@gmail.com",
                leadingIcon = Icons.Default.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryInputField(
                value = contrasena,
                onValueChange = { contrasena = it },
                label = "Contraseña",
                placeholder = "********",
                leadingIcon = Icons.Default.Lock,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            TextAction(
                text = "¿Olvidaste tu contraseña?",
                onClick = {
                    if (correo.isBlank()) {
                        Toast.makeText(contexto, "Ingresa tu correo para recuperar", Toast.LENGTH_SHORT).show()
                    } else if (!correo.lowercase().endsWith("@gmail.com")) {
                        Toast.makeText(contexto, "Ingresa un correo @gmail.com válido", Toast.LENGTH_SHORT).show()
                    } else {
                        alcanceCorrutina.launch {
                            val resultado = repositorio.recuperarContrasena(correo)
                            if (resultado.isSuccess) {
                                Toast.makeText(contexto, "Correo de recuperación enviado", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(contexto, "Error al enviar correo", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (estaCargando) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                PrimaryButton(
                    text = "Iniciar sesión",
                    onClick = {
                        // REQUISITOS SOLICITADOS:
                        // 1. No campos nulos/vacíos
                        if (correo.isBlank() || contrasena.isBlank()) {
                            Toast.makeText(contexto, "Todos los campos tienen que ser llenados", Toast.LENGTH_SHORT).show()
                            return@PrimaryButton
                        }

                        // 2. Correo debe terminar en @gmail.com
                        if (!correo.lowercase().endsWith("@gmail.com")) {
                            Toast.makeText(contexto, "correo no aceptable", Toast.LENGTH_SHORT).show()
                            return@PrimaryButton
                        }

                        estaCargando = true
                        alcanceCorrutina.launch {
                            val resultado = repositorio.iniciarSesion(correo, contrasena)
                            estaCargando = false
                            if (resultado.isSuccess) {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                Toast.makeText(
                                    contexto, 
                                    "Error al ingresar: Verifique sus datos o regístrese",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            DividerWithText(text = "O accede con")

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Acceso institucional (SSO)",
                style = MaterialTheme.typography.labelLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            SSOAccessItem(
                icon = Icons.Default.AccountCircle,
                title = "Google",
                subtitle = "Acceso corporativo",
                onClick = {
                    estaCargando = true
                    val signInIntent = googleSignInClient.signInIntent
                    launcher.launch(signInIntent)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SSOAccessItem(
                icon = Icons.Default.Badge,
                title = "RENIEC Digital",
                subtitle = "Validación de identidad",
                onClick = { }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SSOAccessItem(
                icon = Icons.Default.Security,
                title = "PNP / Serenazgo",
                subtitle = "Acceso personal autorizado",
                onClick = { }
            )

            Spacer(modifier = Modifier.height(24.dp))

            AuthFooter(
                onRegisterClick = { navController.navigate("register") },
                onPrivacyClick = { },
                onTermsClick = { },
                onHelpClick = { }
            )

        }
    }
}
