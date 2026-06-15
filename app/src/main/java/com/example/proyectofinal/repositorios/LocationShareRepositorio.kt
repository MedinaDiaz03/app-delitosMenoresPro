package com.example.proyectofinal.repositorios

import com.example.proyectofinal.modelos.LocationShare
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class LocationShareRepositorio {

    private val db = FirebaseFirestore.getInstance()

    suspend fun iniciarSos(uid: String, lat: Double, lng: Double, duracionMs: Long) {
        val expiraEn = System.currentTimeMillis() + duracionMs
        val data = mapOf(
            "usuarioId" to uid,
            "latitud" to lat,
            "longitud" to lng,
            "activo" to true,
            "timestamp" to System.currentTimeMillis(),
            "expiraEn" to expiraEn
        )
        db.collection("location_sharing").document(uid).set(data).await()
    }

    suspend fun actualizarUbicacion(uid: String, lat: Double, lng: Double) {
        val docRef = db.collection("location_sharing").document(uid)
        val snapshot = docRef.get().await()
        if (snapshot.exists() && snapshot.getBoolean("activo") == true) {
            val expiraEn = snapshot.getLong("expiraEn") ?: 0L
            if (expiraEn > System.currentTimeMillis()) {
                docRef.update(
                    mapOf(
                        "latitud" to lat,
                        "longitud" to lng,
                        "timestamp" to System.currentTimeMillis()
                    )
                ).await()
            } else {
                // Si expiró, desactivar automáticamente
                docRef.update("activo", false).await()
            }
        }
    }

    suspend fun detenerSos(uid: String) {
        db.collection("location_sharing").document(uid).update("activo", false).await()
    }


    fun escuchar(onResult: (List<LocationShare>) -> Unit) {
        db.collection("location_sharing")
            .whereEqualTo("activo", true)
            .addSnapshotListener { snapshot, _ ->
                val ahora = System.currentTimeMillis()
                val lista = snapshot?.documents?.mapNotNull { doc ->
                    val expiraEn = doc.getLong("expiraEn") ?: 0L
                    if (expiraEn > ahora) {
                        doc.toObject(LocationShare::class.java)
                    } else {
                        doc.reference.update("activo", false)
                        null
                    }
                } ?: emptyList()
                onResult(lista.filterNotNull())
            }
    }
}