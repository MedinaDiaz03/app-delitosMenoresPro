package com.example.proyectofinal.repositorios

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class StorageRepositorio {

    // Cambiamos Firebase por la subida nativa a Cloudinary
    suspend fun subirImagen(uri: Uri, path: String): Result<String> {
        return suspendCancellableCoroutine { continuation ->
            MediaManager.get().upload(uri)
                .option("folder", path) // Guarda las fotos en la carpeta "reportes"
                .option("upload_preset", "TU_PRESET_NO_FIRMADO_AQUÍ") // <-- Coloca aquí tu Upload Preset Unsigned
                .callback(object : UploadCallback {

                    override fun onStart(requestId: String) {
                        // Inicia la subida
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        // Progreso opcional de subida
                    }

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        // Cloudinary nos devuelve un mapa con toda la información de la imagen subida.
                        // Obtenemos la URL segura (https)
                        val urlSegura = resultData["secure_url"] as? String
                        if (urlSegura != null) {
                            continuation.resume(Result.success(urlSegura))
                        } else {
                            continuation.resume(Result.failure(Exception("No se encontró la URL en la respuesta de Cloudinary")))
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        continuation.resume(Result.failure(Exception("Error de Cloudinary: ${error.description}")))
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        continuation.resume(Result.failure(Exception("Subida reprogramada: ${error.description}")))
                    }
                })
                .dispatch()
        }
    }
}