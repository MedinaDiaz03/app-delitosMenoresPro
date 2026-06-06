package com.example.proyectofinal.repositorios

import com.example.proyectofinal.modelos.Reporte
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ReporteRepositorio {
    private val db = FirebaseFirestore.getInstance()
    private val reportesCollection = db.collection("reportes")

    suspend fun enviarReporte(reporte: Reporte): Result<Boolean> {
        return try {
            val docRef = reportesCollection.document()
            val reporteConId = reporte.copy(id = docRef.id)
            docRef.set(reporteConId).await()

            // Incrementar contador de reportes del usuario
            // Aquí podríamos actualizar el nivel de confianza si quisiéramos

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerReportes(): List<Reporte> {
        return try {
            reportesCollection
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Reporte::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun obtenerReportesPorUsuario(usuarioId: String): List<Reporte> {
        return try {
            reportesCollection
                .whereEqualTo("usuarioId", usuarioId)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Reporte::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun obtenerConteoReportesUsuario(usuarioId: String): Int {
        return try {
            val snapshot = reportesCollection
                .whereEqualTo("usuarioId", usuarioId)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }

    suspend fun obtenerReportePorId(reporteId: String): Reporte? {
        return try {
            val doc = reportesCollection.document(reporteId).get().await()
            doc.toObject(Reporte::class.java)
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
                val lista = result.toObjects(Reporte::class.java)
                onResult(lista)
            }
    }

    suspend fun confirmarReporte(id: String) {
        try {
            reportesCollection.document(id)
                .update("confirmaciones", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
        } catch (_: Exception) {}
    }

    suspend fun desmentirReporte(id: String) {
        try {
            reportesCollection.document(id)
                .update("desmentidos", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
        } catch (_: Exception) {}
    }
}

// Extensión: obtener un reporte por su ID
// Se usa desde ReportDetailScreen para evitar pasar objetos por navegación