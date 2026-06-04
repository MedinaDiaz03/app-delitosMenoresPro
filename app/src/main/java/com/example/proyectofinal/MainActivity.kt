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
import com.example.proyectofinal.components.screens.HomeScreen
import com.example.proyectofinal.components.screens.LoginScreen
import com.example.proyectofinal.components.screens.ProfileScreen
import com.example.proyectofinal.components.screens.RegisterScreen
import com.example.proyectofinal.components.screens.ReportScreen
import com.example.proyectofinal.components.screens.ReportDetailScreen
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
                        composable("validacion/{reporteId}") { backStackEntry ->
                            val reporteId = backStackEntry.arguments?.getString("reporteId") ?: ""
                            val rolHelper = MockRolHelper()
                            ValidacionEmergenciaScreen(
                                rolHelper = rolHelper,
                                reporteId = reporteId,
                                onValidarComoPolicia = { id ->
                                    // TODO: conectar con la lógica real cuando exista el ViewModel
                                    android.widget.Toast.makeText(this@MainActivity, "Validando reporte $id como policía", Toast.LENGTH_SHORT).show()
                                },
                                onSolicitarValidacion = { id ->
                                    android.widget.Toast.makeText(this@MainActivity, "Solicitando validación para $id", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
class MockRolHelper : RolHelper {
    override fun obtenerRolActual(): Flow<String> = flowOf("comun") // o "policia" para probar
}
