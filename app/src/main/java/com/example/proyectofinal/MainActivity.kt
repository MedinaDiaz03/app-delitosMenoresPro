package com.example.proyectofinal

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proyectofinal.components.screens.AlertsScreen
import com.example.proyectofinal.components.screens.HistorialScreens.HistorialRepoScreen
import com.example.proyectofinal.components.screens.HistorialGlobalScreen
import com.example.proyectofinal.components.screens.HomeCiudadanoScreen
import com.example.proyectofinal.components.screens.HomePoliciaScreen
import com.example.proyectofinal.components.screens.HomeRouterScreen
import com.example.proyectofinal.components.screens.LoginScreen
import com.example.proyectofinal.components.screens.ProfileScreen
import com.example.proyectofinal.components.screens.RegisterScreen
import com.example.proyectofinal.components.screens.ReportScreen
import com.example.proyectofinal.components.screens.ReportDetailScreen
import com.example.proyectofinal.components.screens.SeleccionRolScreen
import com.example.proyectofinal.components.screens.ValidacionEmergenciaScreen
import com.example.proyectofinal.helpers.RolHelper
import com.example.proyectofinal.ui.theme.ProyectoFinalTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()//hace que la app se estire para usar toda la pantalla del cel
        setContent {
            ProyectoFinalTheme {
                Surface {
                    val navController = rememberNavController()//gps de la app

                    NavHost(navController = navController, startDestination = "login") {//el gps consigue el mapa y se declara un punto de partida
                        //Se nombra las rutas existentes y asignando direcciones a cada ruta
                        composable("login")         { LoginScreen(navController) }
                        composable("register")      { RegisterScreen(navController) }
                        composable("home")          { HomeRouterScreen(navController) }
                        composable("report")        { ReportScreen(navController) }
                        composable("alerts")        { AlertsScreen(navController) }
                        composable("profile")       { ProfileScreen(navController) }
                        composable("seleccion_rol") { SeleccionRolScreen(navController) }
                        composable("historial_personal") { HistorialRepoScreen(navController) }
                        composable("historial_repo") { HistorialRepoScreen(navController) }
                        composable("historial_global") { HistorialGlobalScreen(navController) }

                        // El ID del reporte viaja en la ruta → sin riesgo de perder el objeto
                        composable("report_detail/{reporteId}") { backStack ->
                            val reporteId = backStack.arguments?.getString("reporteId") ?: ""
                            ReportDetailScreen(navController = navController, reporteId =reporteId)
                        }
                        composable("validacion/{reporteId}") { backStackEntry ->
                            val reporteId = backStackEntry.arguments?.getString("reporteId") ?: ""
                            val rolHelper = RolHelper()
                            ValidacionEmergenciaScreen(
                                navController = navController,
                                rolHelper = rolHelper,
                                reporteId = reporteId,
                                onValidarComoPolicia = { id ->
                                    android.widget.Toast.makeText(this@MainActivity, "Reporte $id validado por policía", Toast.LENGTH_SHORT).show()
                                },
                                onSolicitarValidacion = { id ->
                                    android.widget.Toast.makeText(this@MainActivity, "Testimonio registrado para $id", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

