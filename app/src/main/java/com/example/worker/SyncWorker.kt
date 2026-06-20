package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d("SyncWorker", "Starting offline sync worker background task...")
        
        // Scope for database callback
        val scope = CoroutineScope(Dispatchers.IO)
        val database = AppDatabase.getDatabase(applicationContext, scope)
        val saleDao = database.saleDao()

        return try {
            val unsyncedSales = saleDao.getUnsyncedSales()
            Log.d("SyncWorker", "Found ${unsyncedSales.size} unsynced sale records.")

            if (unsyncedSales.isEmpty()) {
                Log.d("SyncWorker", "Sync redundant, no logs outstanding.")
                return Result.success()
            }

            // Simulate server network call (e.g. FastAPI /sync endpoint)
            // In a real application, we would map the entities to JSON and execute a POST request.
            delay(1500) // Simulating network latency

            // Now, record success in SQLite
            val now = System.currentTimeMillis()
            unsyncedSales.forEach { sale ->
                saleDao.markSaleSynced(sale.id, now)
                Log.d("SyncWorker", "Sale ${sale.id} marked as synced.")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Sync worker failed with exception: ${e.message}", e)
            Result.retry()
        }
    }
}
