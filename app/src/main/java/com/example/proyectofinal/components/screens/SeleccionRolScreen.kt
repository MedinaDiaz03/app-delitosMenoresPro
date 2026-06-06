package com.example.proyectofinal.components.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyectofinal.helpers.RolHelper

@Composable
fun SeleccionRolScreen(navController: NavController) {

    val context = LocalContext.current
    val rolHelper = remember { RolHelper() }
    var codigo by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "¿Cómo deseas ingresar?",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(
            onClick = {
                rolHelper.setRolCiudadano(context) {
                    navController.navigate("home") {
                        popUpTo("seleccion_rol") { inclusive = true }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ingresar como ciudadano")
        }

        Spacer(modifier = Modifier.height(10.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Ingresar como policía",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = codigo,
            onValueChange = { codigo = it },
            label = { Text("Código de verificación") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = {
                if (codigo == "POLICIA123") {
                    rolHelper.validarCodigoPolicia(context, codigo) {
                        navController.navigate("home") {
                            popUpTo("seleccion_rol") { inclusive = true }
                        }
                    }
                } else {
                    Toast.makeText(context, "Código incorrecto", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Validar como policía")
        }
    }
}
