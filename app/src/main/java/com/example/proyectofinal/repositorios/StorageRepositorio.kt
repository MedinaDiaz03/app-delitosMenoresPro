package com.example.proyectofinal.repositorios

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class StorageRepositorio {

    suspend fun subirImagen(uri: Uri, path: String): Result<String> {
        return try {
            suspendCancellableCoroutine { continuation ->
                android.util.Log.d("CLOUDINARY", "Iniciando subida de: $uri")
                MediaManager.get().upload(uri)
                    .option("folder", path)
                    .option("upload_preset", "reportes_preset")
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {
                            android.util.Log.d("CLOUDINARY", "onStart: $requestId")
                        }
                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                            android.util.Log.d("CLOUDINARY", "onProgress: $bytes/$totalBytes")
                        }
                        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                            val url = resultData["secure_url"] as? String
                            android.util.Log.d("CLOUDINARY", "onSuccess url: $url")
                            if (url != null) continuation.resume(Result.success(url))
                            else continuation.resume(Result.failure(Exception("URL nula")))
                        }
                        override fun onError(requestId: String, error: ErrorInfo) {
                            android.util.Log.e("CLOUDINARY", "onError: ${error.description} code:${error.code}")
                            continuation.resume(Result.failure(Exception("${error.description}")))
                        }
                        override fun onReschedule(requestId: String, error: ErrorInfo) {
                            android.util.Log.e("CLOUDINARY", "onReschedule: ${error.description}")
                            continuation.resume(Result.failure(Exception("Reprogramado: ${error.description}")))
                        }
                    })
                    .dispatch()
            }
        } catch (e: Exception) {
            android.util.Log.e("CLOUDINARY", "Excepcion general: ${e.message}")
            Result.failure(e)
        }
    }
}