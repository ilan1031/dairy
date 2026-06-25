package com.example.data.dao

import androidx.room.*
import com.example.data.entity.CustomerEntity
import com.example.data.entity.PriceConfigEntity
import com.example.data.entity.PriceLogEntity
import com.example.data.entity.SaleEntity
import com.example.data.entity.MilkInventoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllCustomersFlow(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE isDeleted = 0")
    suspend fun getAllCustomers(): List<CustomerEntity>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: String): CustomerEntity?

    @Query("SELECT * FROM customers WHERE isSynced = 0")
    suspend fun getUnsyncedCustomers(): List<CustomerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Query("UPDATE customers SET isSynced = 1, updatedAt = :syncTime WHERE id = :id")
    suspend fun markCustomerSynced(id: String, syncTime: Long)

    @Query("UPDATE customers SET isDeleted = 1, isSynced = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteCustomer(id: String, updatedAt: Long)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun hardDeleteCustomer(id: String)
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales WHERE isDeleted = 0 ORDER BY createdAt DESC")
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

    @Query("SELECT SUM(totalAmount) FROM sales WHERE paymentStatus = 'PENDING' AND isDeleted = 0")
    fun getTotalPendingAmountFlow(): Flow<Double?>

    @Query("SELECT SUM(totalAmount) FROM sales WHERE paymentStatus = 'PAID' AND isDeleted = 0")
    fun getTotalCollectedAmountFlow(): Flow<Double?>

    @Query("SELECT SUM(liters) FROM sales WHERE isDeleted = 0")
    fun getTotalLitersDistributedFlow(): Flow<Double?>

    @Query("UPDATE sales SET isDeleted = 1, isSynced = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteSale(id: String, updatedAt: Long)

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun hardDeleteSale(id: String)
}

@Dao
interface PriceDao {
    @Query("SELECT * FROM price_configs")
    fun getAllPricesFlow(): Flow<List<PriceConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceConfig(config: PriceConfigEntity)

    @Query("SELECT * FROM price_configs WHERE isSynced = 0")
    suspend fun getUnsyncedPrices(): List<PriceConfigEntity>

    @Query("UPDATE price_configs SET isSynced = 1, updatedAt = :syncTime WHERE milkType = :milkType")
    suspend fun markPriceSynced(milkType: String, syncTime: Long)

    @Query("SELECT * FROM price_logs ORDER BY timestamp DESC")
    fun getAllPriceLogsFlow(): Flow<List<PriceLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceLog(log: PriceLogEntity)
}

@Dao
interface MilkInventoryDao {
    @Query("SELECT * FROM milk_inventory ORDER BY dateStr DESC")
    fun getAllInventoryFlow(): Flow<List<MilkInventoryEntity>>

    @Query("SELECT * FROM milk_inventory WHERE dateStr = :dateStr")
    suspend fun getInventoryForDate(dateStr: String): MilkInventoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventory(inventory: MilkInventoryEntity)

    @Query("SELECT * FROM milk_inventory WHERE isSynced = 0")
    suspend fun getUnsyncedInventory(): List<MilkInventoryEntity>

    @Query("UPDATE milk_inventory SET isSynced = 1, updatedAt = :syncTime WHERE dateStr = :dateStr")
    suspend fun markInventorySynced(dateStr: String, syncTime: Long)
}
