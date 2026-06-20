package com.example.data.dao

import androidx.room.*
import com.example.data.entity.CustomerEntity
import com.example.data.entity.PriceConfigEntity
import com.example.data.entity.PriceLogEntity
import com.example.data.entity.SaleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomersFlow(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers")
    suspend fun getAllCustomers(): List<CustomerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomer(id: String)
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY createdAt DESC")
    fun getAllSalesFlow(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleById(id: String): SaleEntity?

    @Query("SELECT * FROM sales WHERE isSynced = 0")
    suspend fun getUnsyncedSales(): List<SaleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity)

    @Query("UPDATE sales SET paymentStatus = :status, paymentType = :paymentType, isSynced = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updatePaymentStatus(id: String, status: String, paymentType: String, updatedAt: Long)

    @Query("UPDATE sales SET isSynced = 1, updatedAt = :syncTime WHERE id = :id")
    suspend fun markSaleSynced(id: String, syncTime: Long)

    @Query("SELECT SUM(totalAmount) FROM sales WHERE paymentStatus = 'PENDING'")
    fun getTotalPendingAmountFlow(): Flow<Double?>

    @Query("SELECT SUM(totalAmount) FROM sales WHERE paymentStatus = 'PAID'")
    fun getTotalCollectedAmountFlow(): Flow<Double?>

    @Query("SELECT SUM(liters) FROM sales")
    fun getTotalLitersDistributedFlow(): Flow<Double?>

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteSale(id: String)
}

@Dao
interface PriceDao {
    @Query("SELECT * FROM price_configs")
    fun getAllPricesFlow(): Flow<List<PriceConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceConfig(config: PriceConfigEntity)

    @Query("SELECT * FROM price_logs ORDER BY timestamp DESC")
    fun getAllPriceLogsFlow(): Flow<List<PriceLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceLog(log: PriceLogEntity)
}
