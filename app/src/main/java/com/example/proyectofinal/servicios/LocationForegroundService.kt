package com.example.proyectofinal.servicios

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ServiceCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.example.proyectofinal.repositorios.LocationRepositorio
import kotlinx.coroutines.*

class LocationForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationRepositorio: LocationRepositorio
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    override fun onCreate() {
        super.onCreate()
        locationRepositorio = LocationRepositorio(applicationContext)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        NotificationHelper.createNotificationChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationHelper.getForegroundNotification(this).build()
        startForeground(1, notification)
        startLocationUpdates()
        return START_STICKY
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            10000L // 10 segundos
        ).setMinUpdateIntervalMillis(5000L)
            .setMaxUpdateDelayMillis(20000L)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    serviceScope.launch {
                        // 1. Guardar ubicación sin dirección (luego se geocodifica aparte)
                        locationRepositorio.guardarUbicacion(location, null)

                        // 2. Geocodificar (opcional, puede ser costoso)
                        val direccion = GeocodingService.getAddressFromLatLng(
                            this@LocationForegroundService,
                            LatLng(location.latitude, location.longitude)
                        )
                        // Actualizar el registro con la dirección (podrías tener otro método)
                        locationRepositorio.actualizarDireccionDeUltimaUbicacion(location, direccion)
                    }
                }
            }
        }
        locationCallback = callback

        try {
            fusedLocationClient?.requestLocationUpdates(
                locationRequest,
                callback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            stopSelf()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient?.removeLocationUpdates(locationCallback!!)
        serviceScope.cancel()
    }
}