package com.example.proyectofinal.repositorios

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import com.example.proyectofinal.servicios.GeocodingService
import com.example.proyectofinal.servicios.LocationForegroundService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore



class LocationRepositorio(context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private var isServiceRunning = false
    fun iniciarMonitoreoUbicacion(context: Context) {
        if (!isServiceRunning) {
            val intent = Intent(context, LocationForegroundService::class.java)
            ContextCompat.startForegroundService(context, intent) // ← cambia esto
            isServiceRunning = true
        }
    }

    fun detenerMonitoreoUbicacion(context: Context) {
        val intent = Intent(context, LocationForegroundService::class.java)
        context.stopService(intent)
        isServiceRunning = false
    }

    suspend fun guardarUbicacion(location: Location, direccion: String?) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val data = hashMapOf(
            "lat" to location.latitude,
            "lng" to location.longitude,
            "direccion" to direccion,
            "timestamp" to System.currentTimeMillis()
        )
        firestore.collection("ubicaciones").document(userId).collection("historial").add(data)
    }

    // Método auxiliar para actualizar dirección (opcional)
    suspend fun actualizarDireccionDeUltimaUbicacion(location: Location, direccion: String?) {
        // Implementación simple: podrías guardar la ubicación otra vez con la dirección actualizada
        guardarUbicacion(location, direccion)
    }
    @SuppressLint("MissingPermission")
    suspend fun obtenerUbicacionActual(): Location? {
        return try {
            // Intentar last known location primero (instantáneo)
            val ultimaUbicacion = fusedLocationClient.lastLocation.await()
            if (ultimaUbicacion != null &&
                System.currentTimeMillis() - ultimaUbicacion.time < 30_000L
            ) {
                return ultimaUbicacion
            }
            // Fallback: solicitar ubicación actual con precisión balanceada + timeout 5s
            withTimeoutOrNull(5_000L) {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    null
                ).await()
            }
        } catch (e: Exception) {
            null
        }
    }

}
