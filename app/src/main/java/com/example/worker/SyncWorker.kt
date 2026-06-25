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
        Log.d("SyncWorker", "Starting network sync worker background task...")
        
        val scope = CoroutineScope(Dispatchers.IO)
        val database = AppDatabase.getDatabase(applicationContext, scope)
        val repository = com.example.data.repository.Repository(
            customerDao = database.customerDao(),
            saleDao = database.saleDao(),
            priceDao = database.priceDao(),
            milkInventoryDao = database.milkInventoryDao()
        )

        return try {
            val success = repository.syncUnsyncedData(applicationContext)
            if (success) {
                Log.d("SyncWorker", "Sync worker completed successfully.")
                Result.success()
            } else {
                Log.d("SyncWorker", "Sync worker completed with no network or some errors.")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("SyncWorker", "Sync worker failed with exception: ${e.message}", e)
            Result.retry()
        }
    }
}
