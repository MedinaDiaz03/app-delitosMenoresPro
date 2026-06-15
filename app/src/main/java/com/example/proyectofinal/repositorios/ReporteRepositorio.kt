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

    // ── VOTACIÓN (sistema unificado) ──────────────────────────────────────────

    suspend fun agregarVoto(
        reporteId: String,
        usuarioId: String,
        voto: Boolean,        // true = real, false = falsa alarma
        esPolicia: Boolean
    ): Result<String> {
        return try {
            // Leer estado actual del reporte
            val reporteDoc = reportesCollection.document(reporteId).get().await()
            val reporte = reporteDoc.toObject(Reporte::class.java)
                ?: return Result.failure(Exception("Reporte no encontrado"))

            // Guard ciudadano: solo en_revision o activo (legacy)
            if (!esPolicia && reporte.estado !in listOf("en_revision", "activo"))
                return Result.failure(Exception("Este reporte ya no acepta votos ciudadanos"))

            // Guard ciudadano: no después de que haya votado un policía
            if (!esPolicia && reporte.policiaHaVotado)
                return Result.failure(Exception("Reporte cerrado por autoridad. No se aceptan más votos."))

            // Guard: sin votos duplicados
            val docId = "${reporteId}_${usuarioId}"
            if (db.collection("validaciones").document(docId).get().await().exists())
                return Result.failure(Exception("Ya has votado en este reporte"))

            // Guardar la validación
            db.collection("validaciones").document(docId).set(
                mapOf(
                    "id" to docId,
                    "reporteId" to reporteId,
                    "usuarioId" to usuarioId,
                    "voto" to voto,
                    "esPolicia" to esPolicia,
                    "timestamp" to Timestamp.now()
                )
            ).await()

            if (esPolicia) {
                // Voto de autoridad: cambio inmediato e irreversible
                val nuevoEstado = if (voto) "verificado" else "falso"
                reportesCollection.document(reporteId).update(
                    mapOf(
                        "estado" to nuevoEstado,
                        "policiaHaVotado" to true,
                        "estadoFinalPorPolicia" to nuevoEstado
                    )
                ).await()
            } else {
                // Voto ciudadano: incrementar contadores
                val campoVoto = if (voto) "votosReales" else "votosFalsos"
                reportesCollection.document(reporteId).update(
                    mapOf(
                        "totalVotosCiudadanos" to FieldValue.increment(1),
                        campoVoto to FieldValue.increment(1)
                    )
                ).await()

                // Re-leer para verificar si se alcanzó el umbral
                val updated = reportesCollection.document(reporteId).get().await()
                    .toObject(Reporte::class.java)
                if (updated != null
                    && updated.totalVotosCiudadanos >= 3
                    && !updated.policiaHaVotado
                    && updated.estado in listOf("en_revision", "activo")
                ) {
                    val nuevoEstado = if (updated.votosReales > updated.votosFalsos) "verificado" else "falso"
                    reportesCollection.document(reporteId).update("estado", nuevoEstado).await()
                    ajustarConfianzaValidadores(reporteId, votaronReal = (nuevoEstado == "verificado"), delta = 2)
                }
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
            val docs = db.collection("validaciones")
                .whereEqualTo("reporteId", reporteId)
                .get().await()
            docs.documents
                .filter { it.getBoolean("esReal") == votaronReal || it.getBoolean("voto") == votaronReal }
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
                .whereIn("estado", listOf("en_revision", "activo", "verificado"))
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
