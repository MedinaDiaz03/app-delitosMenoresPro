package com.example.proyectofinal

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.runtime.LaunchedEffect
import com.example.proyectofinal.components.screens.*
import com.example.proyectofinal.helpers.RolHelper
import com.example.proyectofinal.ui.theme.ProyectoFinalTheme
import com.example.proyectofinal.viewmodels.MapCenteringViewModel
import com.google.android.gms.maps.model.LatLng

class MainActivity : ComponentActivity() {

    private val mapCenteringViewModel: MapCenteringViewModel by viewModels()

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleSosIntent(intent)
    }

    private fun handleSosIntent(i: android.content.Intent) {
        val lat = if (i.hasExtra("sos_lat")) i.getDoubleExtra("sos_lat", 0.0) else null
        val lng = if (i.hasExtra("sos_lng")) i.getDoubleExtra("sos_lng", 0.0) else null
        if (lat != null && lng != null) {
            mapCenteringViewModel.emitCenter(LatLng(lat, lng))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            ProyectoFinalTheme {
                Surface {
                    val navController = rememberNavController()

                    // Capturar click de notificación al arrancar la Activity
                    LaunchedEffect(intent) {
                        val reporteId = intent.getStringExtra("reporteId")
                        if (!reporteId.isNullOrEmpty()) {
                            navController.navigate("report_detail/$reporteId")
                        }
                        handleSosIntent(intent)
                    }

                    NavHost(navController = navController, startDestination = "login") {
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

