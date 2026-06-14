package com.example.proyectofinal.components.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyectofinal.components.auth.*
import com.example.proyectofinal.repositorios.AutenticacionRepositorio
import kotlinx.coroutines.launch
import android.app.Activity
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

    var correoError by remember { mutableStateOf<String?>(null) }
    var contrasenaError by remember { mutableStateOf<String?>(null) }

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

    LaunchedEffect(Unit) {
        if (repositorio.obtenerUsuarioActual() != null) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    fun validar(): Boolean {
        var valido = true
        correoError = when {
            correo.isBlank() -> "El correo es obligatorio"
            !correo.lowercase().endsWith("@gmail.com") -> "Ingresa un correo @gmail.com válido"
            else -> null
        }
        contrasenaError = when {
            contrasena.isBlank() -> "La contraseña es obligatoria"
            else -> null
        }
        if (correoError != null || contrasenaError != null) valido = false
        return valido
    }

    Scaffold(containerColor = Color(0xFFF8FAFC)) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(56.dp))

            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Iniciar sesión",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ingresa tus credenciales para continuar",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF475569),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            PrimaryInputField(
                value = correo,
                onValueChange = { correo = it; correoError = null },
                label = "Correo electrónico",
                placeholder = "usuario@gmail.com",
                leadingIcon = Icons.Default.Email,
                errorMessage = correoError
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryInputField(
                value = contrasena,
                onValueChange = { contrasena = it; contrasenaError = null },
                label = "Contraseña",
                placeholder = "••••••••",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                errorMessage = contrasenaError
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        if (correo.isBlank()) {
                            correoError = "Ingresa tu correo para recuperar"
                        } else if (!correo.lowercase().endsWith("@gmail.com")) {
                            correoError = "Ingresa un correo @gmail.com válido"
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
            }

            Spacer(modifier = Modifier.height(28.dp))

            if (estaCargando) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else {
                PrimaryButton(
                    text = "Iniciar sesión",
                    onClick = {
                        if (!validar()) return@PrimaryButton
                        estaCargando = true
                        alcanceCorrutina.launch {
                            val resultado = repositorio.iniciarSesion(correo, contrasena)
                            estaCargando = false
                            if (resultado.isSuccess) {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                contrasenaError = "Correo o contraseña incorrectos"
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            DividerWithText(text = "O continúa con")

            Spacer(modifier = Modifier.height(16.dp))

            GoogleButton(
                onClick = {
                    estaCargando = true
                    launcher.launch(googleSignInClient.signInIntent)
                }
            )

            Spacer(modifier = Modifier.height(40.dp))

            Row(horizontalArrangement = Arrangement.Center) {
                Text(
                    text = "¿No tienes cuenta? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF475569)
                )
                Text(
                    text = "Regístrate",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { navController.navigate("register") }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
