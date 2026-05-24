package com.example.proyectofinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
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
import com.example.proyectofinal.components.screens.ReportDetailScreen
import com.example.proyectofinal.ui.theme.ProyectoFinalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProyectoFinalTheme {
                Surface {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "login") {

                        composable("login")         { LoginScreen(navController) }
                        composable("register")      { RegisterScreen(navController) }
                        composable("home")          { HomeScreen(navController) }
                        composable("report")        { ReportScreen(navController) }
                        composable("alerts")        { AlertsScreen(navController) }
                        composable("profile")       { ProfileScreen(navController) }
                        composable("historial_repo") { HistorialRepoScreen(navController) }

                        // El ID del reporte viaja en la ruta → sin riesgo de perder el objeto
                        composable("report_detail/{reporteId}") { backStack ->
                            val reporteId = backStack.arguments?.getString("reporteId") ?: ""
                            ReportDetailScreen(navController = navController, reporteId =reporteId)
                        }
                    }
                }
            }
        }
    }
}