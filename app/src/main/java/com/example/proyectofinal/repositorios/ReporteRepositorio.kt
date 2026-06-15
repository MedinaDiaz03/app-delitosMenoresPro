package com.example.proyectofinal.repositorios

import com.example.proyectofinal.modelos.Reporte
import com.example.proyectofinal.modelos.Validacion
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlin.math.*

class ReporteRepositorio {
    private val db = FirebaseFirestore.getInstance()
    private val reportesCollection = db.collection("reportes")

    suspend fun enviarReporte(reporte: Reporte): Result<Boolean> {
        return try {
            val docRef = reportesCollection.document()
            val reporteConId = reporte.copy(id = docRef.id)
            docRef.set(reporteConId).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerReportes(): List<Reporte> {
        return try {
            reportesCollection
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get().await()
                .toObjects(Reporte::class.java)
        } catch (_: Exception) { emptyList() }
    }

    suspend fun obtenerReportesPorUsuario(usuarioId: String): List<Reporte> {
        return try {
            // Intentar con ordenamiento (requiere índice compuesto en Firebase)
            reportesCollection
                .whereEqualTo("usuarioId", usuarioId)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get().await()
                .toObjects(Reporte::class.java)
        } catch (e: Exception) {
            // Fallback: Si el índice no existe, obtenemos sin ordenar y ordenamos en memoria
            try {
                reportesCollection
                    .whereEqualTo("usuarioId", usuarioId)
                    .get().await()
                    .toObjects(Reporte::class.java)
                    .sortedByDescending { it.fecha }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    suspend fun obtenerConteoReportesUsuario(usuarioId: String): Int {
        return try {
            reportesCollection.whereEqualTo("usuarioId", usuarioId).get().await().size()
        } catch (_: Exception) { 0 }
    }

    suspend fun obtenerReportePorId(reporteId: String): Reporte? {
        return try {
            reportesCollection.document(reporteId).get().await().toObject(Reporte::class.java)
        } catch (_: Exception) { null }
    }

    fun actualizarEstadoReporte(reporteId: String, nuevoEstado: String) {
        reportesCollection.document(reporteId).update("estado", nuevoEstado)
    }

    fun escucharReportes(onResult: (List<Reporte>) -> Unit) {
        reportesCollection
            // Quitamos el filtro de "activo" para que el historial global muestre todo
            .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                val lista = snapshot?.toObjects(Reporte::class.java) ?: emptyList()
                onResult(lista)
            }
    }

    suspend fun validarComoPolicia(reporteId: String, esReal: Boolean): Result<Boolean> {
        return try {
            val nuevoEstado = if (esReal) "verificado" else "falso"
            reportesCollection.document(reporteId).update("estado", nuevoEstado).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── VALIDACIONES ──────────────────────────────────────────────────────────

    suspend fun registrarValidacion(
        reporteId: String,
        usuarioId: String,
        esReal: Boolean
    ): Result<String> {
        return try {
            if (usuarioId.isBlank()) return Result.failure(Exception("Usuario no autenticado"))

            val docId = "${reporteId}_${usuarioId}"

            // PASO 1: Guardar la validación. Esto DEBE funcionar si las reglas permiten
            // crear documentos en la colección "validaciones" donde el ID contiene el UID del usuario.
            db.collection("validaciones").document(docId).set(
                mapOf(
                    "id" to docId,
                    "reporteId" to reporteId,
                    "usuarioId" to usuarioId,
                    "esReal" to esReal,
                    "fecha" to Timestamp.now()
                )
            ).await()

            // PASO 2: Intentar actualizar contadores. Si las reglas de Firebase deniegan
            // el acceso (PERMISSION_DENIED) porque el usuario no es dueño del reporte,
            // atrapamos el error y devolvemos éxito. Tu voto ya se guardó arriba.
            try {
                val campo = if (esReal) "validacionesCount" else "rechazosCount"
                reportesCollection.document(reporteId).update(campo, FieldValue.increment(1)).await()

                val reporteDoc = reportesCollection.document(reporteId).get().await()
                val validaciones = reporteDoc.getLong("validacionesCount")?.toInt() ?: 0
                val rechazos = reporteDoc.getLong("rechazosCount")?.toInt() ?: 0
                val estadoActual = reporteDoc.getString("estado") ?: "activo"

                if (estadoActual == "activo") {
                    when {
                        validaciones >= 3 -> {
                            reportesCollection.document(reporteId).update("estado", "verificado").await()
                            ajustarConfianzaValidadores(reporteId, votaronReal = true, delta = 2)
                        }
                        rechazos >= 3 -> {
                            reportesCollection.document(reporteId).update("estado", "falso").await()
                            ajustarConfianzaValidadores(reporteId, votaronReal = false, delta = -2)
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignorar fallos de permisos al actualizar el documento de otra persona
                android.util.Log.w("ReporteRepositorio", "No se pudo actualizar contador por seguridad, pero voto guardado: ${e.message}")
            }

            Result.success("ok")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun yaVoto(reporteId: String, usuarioId: String): Boolean {
        return try {
            db.collection("validaciones")
                .document("${reporteId}_${usuarioId}")
                .get().await().exists()
        } catch (_: Exception) { false }
    }

    private suspend fun ajustarConfianzaValidadores(
        reporteId: String,
        votaronReal: Boolean,
        delta: Int
    ) {
        try {
            // Query por un solo campo (sin índice compuesto), filtramos esReal en código
            val docs = db.collection("validaciones")
                .whereEqualTo("reporteId", reporteId)
                .get().await()
            docs.documents
                .filter { it.getBoolean("esReal") == votaronReal }
                .forEach { doc ->
                    val uid = doc.getString("usuarioId") ?: return@forEach
                    db.collection("usuarios").document(uid)
                        .update("nivelConfianza", FieldValue.increment(delta.toLong()))
                }
        } catch (_: Exception) {}
    }

    // ── ALERTAS CERCANAS ──────────────────────────────────────────────────────

    suspend fun obtenerReportesEnRadio(lat: Double, lng: Double, radioMetros: Double = 1000.0): List<Reporte> {
        return try {
            val todos = reportesCollection
                .whereEqualTo("estado", "activo")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .limit(200)
                .get().await()
                .toObjects(Reporte::class.java)

            todos.filter { r ->
                r.latitud != 0.0 || r.longitud != 0.0
            }.filter { r ->
                calcularDistanciaMetros(lat, lng, r.latitud, r.longitud) <= radioMetros
            }.sortedBy {
                calcularDistanciaMetros(lat, lng, it.latitud, it.longitud)
            }
        } catch (_: Exception) { emptyList() }
    }

    fun calcularDistanciaMetros(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371e3
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val dPhi = Math.toRadians(lat2 - lat1)
        val dLambda = Math.toRadians(lon2 - lon1)
        val a = sin(dPhi / 2).pow(2) + cos(phi1) * cos(phi2) * sin(dLambda / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    // ── ALERTAS POR USUARIO (subcolección) ───────────────────────────────────

    suspend fun guardarAlertaUsuario(usuarioId: String, reporteId: String, categoria: String) {
        try {
            db.collection("usuarios").document(usuarioId)
                .collection("alertas").document(reporteId)
                .set(
                    mapOf(
                        "reporteId" to reporteId,
                        "categoria" to categoria,
                        "leido" to false,
                        "fecha" to Timestamp.now()
                    )
                ).await()
        } catch (_: Exception) {}
    }

    suspend fun marcarAlertaLeida(usuarioId: String, reporteId: String) {
        try {
            db.collection("usuarios").document(usuarioId)
                .collection("alertas").document(reporteId)
                .update("leido", true).await()
        } catch (_: Exception) {}
    }
}
