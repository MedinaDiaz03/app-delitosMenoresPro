package com.example.proyectofinal.repositorios

import com.example.proyectofinal.modelos.LocationShare
import com.google.firebase.firestore.FirebaseFirestore

class LocationShareRepositorio {

    private val db = FirebaseFirestore.getInstance()

    fun actualizarUbicacion(uid: String, lat: Double, lng: Double) {
        db.collection("location_sharing")
            .document(uid)
            .set(
                mapOf(
                    "usuarioId" to uid,
                    "latitud" to lat,
                    "longitud" to lng,
                    "activo" to true,
                    "timestamp" to System.currentTimeMillis()
                )
            )
    }

    fun detener(uid: String) {
        db.collection("location_sharing")
            .document(uid)
            .update("activo", false)
    }

    fun escuchar(onResult: (List<LocationShare>) -> Unit) {
        db.collection("location_sharing")
            .whereEqualTo("activo", true)
            .addSnapshotListener { value, _ ->
                val lista = value?.toObjects(LocationShare::class.java) ?: emptyList()
                onResult(lista)
            }
    }
}
