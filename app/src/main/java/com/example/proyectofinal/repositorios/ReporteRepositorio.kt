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
            reportesCollection
                .whereEqualTo("usuarioId", usuarioId)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get().await()
                .toObjects(Reporte::class.java)
        } catch (_: Exception) { emptyList() }
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

    fun obtenerTodosLosReportes(onResult: (List<Reporte>) -> Unit) {
        reportesCollection
            .orderBy("fecha", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                onResult(result.toObjects(Reporte::class.java))
            }
    }

    suspend fun confirmarReporte(id: String) {
        try {
            reportesCollection.document(id)
                .update("confirmaciones", FieldValue.increment(1)).await()
        } catch (_: Exception) {}
    }

    suspend fun desmentirReporte(id: String) {
        try {
            reportesCollection.document(id)
                .update("desmentidos", FieldValue.increment(1)).await()
        } catch (_: Exception) {}
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

            // Escribir directamente sin pre-check GET — idempotente por docId compuesto.
            // Evita el READ extra que puede ser bloqueado por reglas de Firestore.
            db.collection("validaciones").document(docId).set(
                mapOf(
                    "id" to docId,
                    "reporteId" to reporteId,
                    "usuarioId" to usuarioId,
                    "esReal" to esReal,
                    "fecha" to Timestamp.now()
                )
            ).await()

            // Actualizar contadores y estado del reporte — best-effort, no bloquea el resultado
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
                            ajustarConfianzaValidadores(reporteId, votaronReal = true, delta = -2)
                        }
                    }
                }
            } catch (_: Exception) { /* Contadores son best-effort; la validación ya fue guardada */ }

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
