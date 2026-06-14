package com.example.proyectofinal.repositorios

import com.example.proyectofinal.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class StorageRepositorio {

    // Upload directo vía HTTP a Cloudinary — sin WorkManager, sin cola de background service
    suspend fun subirImagen(bytes: ByteArray, path: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
                val boundary = "Boundary${System.currentTimeMillis()}"
                val url = URL("https://api.cloudinary.com/v1_1/$cloudName/image/upload")

                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.connectTimeout = 15_000
                conn.readTimeout = 45_000
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

                conn.outputStream.use { out ->
                    fun part(name: String, value: String) {
                        out.write("--$boundary\r\nContent-Disposition: form-data; name=\"$name\"\r\n\r\n$value\r\n".toByteArray())
                    }
                    part("upload_preset", "reportes_preset")
                    part("folder", path)
                    out.write("--$boundary\r\nContent-Disposition: form-data; name=\"file\"; filename=\"img.jpg\"\r\nContent-Type: image/jpeg\r\n\r\n".toByteArray())
                    out.write(bytes)
                    out.write("\r\n--$boundary--\r\n".toByteArray())
                }

                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    val body = conn.inputStream.bufferedReader().readText()
                    Result.success(JSONObject(body).getString("secure_url"))
                } else {
                    val err = conn.errorStream?.bufferedReader()?.readText() ?: conn.responseCode.toString()
                    Result.failure(Exception("Cloudinary error: $err"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
