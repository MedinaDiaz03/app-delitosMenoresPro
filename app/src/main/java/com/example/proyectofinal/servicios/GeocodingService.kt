package com.example.proyectofinal.servicios

import android.content.Context
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

object GeocodingService {
    suspend fun getAddressFromLatLng(context: Context, latLng: LatLng): String? =
        withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val addressLine = address.getAddressLine(0)
                    return@withContext addressLine
                }
                null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
}