package com.example.proyectofinal.components.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.proyectofinal.modelos.Usuario
import com.example.proyectofinal.repositorios.AutenticacionRepositorio
import kotlinx.coroutines.launch
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*

@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repositorio = remember { AutenticacionRepositorio() }

    val auth = FirebaseAuth.getInstance()
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("738190982265-r5q3tl92qurcgqn362ds9acghn1pist3.apps.googleusercontent.com")
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    var nombreCompleto by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var aceptoTerminos by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var codigoPolicia by remember { mutableStateOf("") }

    var nombreError by remember { mutableStateOf<String?>(null) }
    var correoError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var codigoError by remember { mutableStateOf<String?>(null) }
    var terminosError by remember { mutableStateOf(false) }

    fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    scope.launch {
                        val usuario = repositorio.asegurarUsuarioEnFirestore()
                        isLoading = false
                        Toast.makeText(context, "Bienvenido: ${auth.currentUser?.displayName}", Toast.LENGTH_SHORT).show()
                        
                        if (usuario != null && !usuario.rol.isNullOrBlank()) {
                            navController.navigate("home") {
                                popUpTo("register") { inclusive = true }
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            navController.navigate("seleccion_rol") {
                                popUpTo("register") { inclusive = true }
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    }
                } else {
                    isLoading = false
                    Toast.makeText(context, "Error en Firebase: ${task.exception?.message}", Toast.LENGTH_LONG).show()
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
                isLoading = false
                Toast.makeText(context, "Error de Google: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        } else {
            isLoading = false
        }
    }

    fun soloLetras(texto: String): Boolean = texto.all { it.isLetter() || it.isWhitespace() }

    fun validar(): Boolean {
        nombreError = if (nombreCompleto.isBlank()) "El nombre es obligatorio" else null
        correoError = when {
            correo.isBlank() -> "El correo es obligatorio"
            !correo.lowercase().endsWith("@gmail.com") -> "Ingresa un correo @gmail.com válido"
            else -> null
        }
        passwordError = when {
            password.isBlank() -> "La contraseña es obligatoria"
            password.length < 6 -> "Mínimo 6 caracteres"
            else -> null
        }
        confirmPasswordError = when {
            confirmPassword.isBlank() -> "Confirma tu contraseña"
            confirmPassword != password -> "Las contraseñas no coinciden"
            else -> null
        }
        terminosError = !aceptoTerminos
        return nombreError == null && correoError == null &&
               passwordError == null && confirmPasswordError == null && aceptoTerminos
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

            Spacer(modifier = Modifier.height(48.dp))

            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Crear cuenta",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Únete y contribuye a la seguridad ciudadana",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF475569),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            PrimaryInputField(
                value = nombreCompleto,
                onValueChange = { if (soloLetras(it)) { nombreCompleto = it; nombreError = null } },
                label = "Nombre completo",
                placeholder = "Ej. Juan Pérez",
                leadingIcon = Icons.Default.Person,
                modifier = Modifier.fillMaxWidth(),
                errorMessage = nombreError
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryInputField(
                value = correo,
                onValueChange = { correo = it; correoError = null },
                label = "Correo electrónico",
                placeholder = "nombre@gmail.com",
                leadingIcon = Icons.Default.Email,
                errorMessage = correoError
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryInputField(
                value = codigoPolicia,
                onValueChange = { codigoPolicia = it; codigoError = null },
                label = "Código de oficial (opcional)",
                placeholder = "Solo si eres policía",
                leadingIcon = Icons.Default.Security,
                errorMessage = codigoError
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryInputField(
                value = password,
                onValueChange = { password = it; passwordError = null },
                label = "Contraseña",
                placeholder = "••••••••",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                errorMessage = passwordError
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryInputField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; confirmPasswordError = null },
                label = "Confirmar contraseña",
                placeholder = "••••••••",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                errorMessage = confirmPasswordError
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                TermsCheckbox(
                    checked = aceptoTerminos,
                    onCheckedChange = { aceptoTerminos = it; terminosError = false }
                )
                if (terminosError) {
                    Text(
                        text = "Debes aceptar los términos para continuar",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 12.dp, top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else {
                PrimaryButton(
                    text = "Crear cuenta",
                    onClick = {
                        if (!validar()) return@PrimaryButton

                        isLoading = true
                        val esCodigoValido = codigoPolicia == "POLICIA123"

                        if (codigoPolicia.isNotBlank() && !esCodigoValido) {
                            codigoError = "Código de oficial incorrecto"
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
                                correoError = "Este correo ya está registrado o no es válido"
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            DividerWithText(text = "O regístrate con")

            Spacer(modifier = Modifier.height(16.dp))

            GoogleButton(onClick = { 
                isLoading = true
                launcher.launch(googleSignInClient.signInIntent)
            })

            Spacer(modifier = Modifier.height(36.dp))

            Row(horizontalArrangement = Arrangement.Center) {
                Text(
                    text = "¿Ya tienes cuenta? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF475569)
                )
                Text(
                    text = "Iniciar sesión",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { navController.popBackStack() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
