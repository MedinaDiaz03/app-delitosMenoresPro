package com.example.proyectofinal.components.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AuthFooter(
    onRegisterClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onTermsClick: () -> Unit,
    onHelpClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row {
            Text(
                text = "¿No tienes cuenta? ",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Regístrate",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onRegisterClick() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Privacidad",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.clickable { onPrivacyClick() }
            )
            Text(
                text = "Términos",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.clickable { onTermsClick() }
            )
            Text(
                text = "Ayuda",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.clickable { onHelpClick() }
            )
        }
    }
}