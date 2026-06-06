package com.example.proyectofinal.components.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.proyectofinal.ui.theme.GreenPrimary

@Composable
fun BottomNavigationBar(navController: NavController, esPolicia: Boolean = false) {
    val items = remember(esPolicia) {
        val baseItems = mutableListOf(
            NavigationItem("home", "Inicio", Icons.Default.Map)
        )

        // SOLO el ciudadano ve el botón de Reportar
        if (!esPolicia) {
            baseItems.add(NavigationItem("report", "Reportar", Icons.Default.AddCircle))
        }

        baseItems.add(NavigationItem("alerts", "Alertas", Icons.Default.Notifications))
        baseItems.add(NavigationItem("profile", "Perfil", Icons.Default.Person))

        baseItems
    }

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = GreenPrimary
    ) {
        val navBackStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry.value?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GreenPrimary,
                    selectedTextColor = GreenPrimary,
                    unselectedIconColor = MaterialTheme.colorScheme.outline,
                    unselectedTextColor = MaterialTheme.colorScheme.outline,
                    indicatorColor = GreenPrimary.copy(alpha = 0.1f)
                )
            )
        }
    }
}

data class NavigationItem(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
