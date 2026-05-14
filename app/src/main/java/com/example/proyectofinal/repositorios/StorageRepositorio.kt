package com.example.proyectofinal.repositorios

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class StorageRepositorio {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    suspend fun subirImagen(uri: Uri, path: String): Result<String> {
        return try {
            val imagenRef = storageRef.child("$path/${UUID.randomUUID()}.jpg")
            imagenRef.putFile(uri).await()
            val downloadUrl = imagenRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
