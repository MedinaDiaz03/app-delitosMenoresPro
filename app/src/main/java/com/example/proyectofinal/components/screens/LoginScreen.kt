package com.example.proyectofinal.components.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyectofinal.components.auth.*
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.example.proyectofinal.components.auth.SSOAccessItem

import com.example.proyectofinal.components.auth.AuthFooter
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll





@Composable
fun LoginScreen(navController: NavController) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())// Para desplazar la pantalla
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
                value = email,
                onValueChange = { email = it },
                label = "Correo o DNI",
                placeholder = "correo@ejemplo.com",
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

            Spacer(modifier = Modifier.height(24.dp))


            TextAction(
                text = "¿Olvidaste tu contraseña?",
                onClick = { /* navegar luego */ }
            )

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = "Iniciar sesión",
                onClick = { navController.navigate("home") }
            )

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
                onClick = { }
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
