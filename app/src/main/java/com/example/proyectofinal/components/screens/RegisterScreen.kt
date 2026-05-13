package com.example.proyectofinal.components.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyectofinal.components.auth.*

@Composable
fun RegisterScreen(navController: NavController) {

    var nombres by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var aceptoTerminos by remember { mutableStateOf(false) }

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

            // Nombres y Apellidos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Aca llamamos al microcomponente PrimaryInputField (caja para escribir)
                PrimaryInputField(
                    value = nombres,
                    onValueChange = { nombres = it },
                    label = "Nombres",
                    placeholder = "Ej. Juan",
                    leadingIcon = Icons.Default.Person,
                    modifier = Modifier.weight(1f)
                )

                PrimaryInputField(
                    value = apellidos,
                    onValueChange = { apellidos = it },
                    label = "Apellidos",
                    placeholder = "Ej. Pérez",
                    leadingIcon = Icons.Default.Person,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryInputField(
                value = dni,
                onValueChange = { dni = it },
                label = "DNI",
                placeholder = "Número de documento",
                leadingIcon = Icons.Default.Badge
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryInputField(
                value = correo,
                onValueChange = { correo = it },
                label = "Correo electrónico",
                placeholder = "nombre@ejemplo.com",
                leadingIcon = Icons.Default.Email
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

            PrimaryButton(
                text = "Crear cuenta",
                onClick = { navController.navigate("home") }
            )

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