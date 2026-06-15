package com.example.proyectofinal.servicios

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.proyectofinal.repositorios.ReporteRepositorio

class ExpirarReportesWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val repositorio = ReporteRepositorio()
        val resultado = repositorio.eliminarReportesExpirados()
        
        return if (resultado.isSuccess) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}
