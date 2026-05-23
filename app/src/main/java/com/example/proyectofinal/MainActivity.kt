package com.example.proyectofinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proyectofinal.components.screens.AlertsScreen
import com.example.proyectofinal.components.screens.HistorialScreens.HistorialRepoScreen
import com.example.proyectofinal.components.screens.HomeScreen
import com.example.proyectofinal.components.screens.LoginScreen
import com.example.proyectofinal.components.screens.ProfileScreen
import com.example.proyectofinal.components.screens.RegisterScreen
import com.example.proyectofinal.components.screens.ReportScreen
import com.example.proyectofinal.components.screens.ReportDetailScreen // Importamos la nueva pantalla
import com.example.proyectofinal.modelos.Reporte
import com.example.proyectofinal.ui.theme.ProyectoFinalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProyectoFinalTheme {
                Surface {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "login"
                    ) {
                        composable(route = "login") {
                            LoginScreen(navController)
                        }
                        composable(route = "register") {
                            RegisterScreen(navController)
                        }
                        composable(route = "home") {
                            HomeScreen(navController)
                        }
                        composable(route = "report") {
                            ReportScreen(navController)
                        }
                        composable(route = "alerts") {
                            AlertsScreen(navController)
                        }
                        composable(route = "profile") {
                            ProfileScreen(navController)
                        }
                        composable(route = "historial_repo") {
                            HistorialRepoScreen(navController)
                        }

                        // Reemplaza TU bloque de "report_detail" en MainActivity.kt por este:
                        composable(route = "report_detail") {
                            // Buscamos el objeto de manera directa en el BackStack actual
                            val reporteData = remember {
                                navController.previousBackStackEntry?.savedStateHandle?.get<Reporte>("reporte_objeto")
                            }

                            if (reporteData != null) {
                                ReportDetailScreen(navController = navController, reporte = reporteData)
                            } else {
                                // Si el sistema de navegación pierde el objeto por milisegundos,
                                // evitamos el crash regresando al usuario de forma segura.
                                LaunchedEffect(Unit) {
                                    navController.popBackStack()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}