package com.example.data.repository

import com.example.data.dao.CustomerDao
import com.example.data.dao.PriceDao
import com.example.data.dao.SaleDao
import com.example.data.dao.MilkInventoryDao
import com.example.data.entity.CustomerEntity
import com.example.data.entity.PriceConfigEntity
import com.example.data.entity.PriceLogEntity
import com.example.data.entity.SaleEntity
import com.example.data.entity.MilkInventoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class Repository(
    private val customerDao: CustomerDao,
    private val saleDao: SaleDao,
    private val priceDao: PriceDao,
    private val milkInventoryDao: MilkInventoryDao
) {
    val customersFlow: Flow<List<CustomerEntity>> = customerDao.getAllCustomersFlow()
    val salesFlow: Flow<List<SaleEntity>> = saleDao.getAllSalesFlow()
    val pricesFlow: Flow<List<PriceConfigEntity>> = priceDao.getAllPricesFlow()
    val priceLogsFlow: Flow<List<PriceLogEntity>> = priceDao.getAllPriceLogsFlow()
    val inventoryFlow: Flow<List<MilkInventoryEntity>> = milkInventoryDao.getAllInventoryFlow()

    val totalPendingFlow: Flow<Double?> = saleDao.getTotalPendingAmountFlow()
    val totalCollectedFlow: Flow<Double?> = saleDao.getTotalCollectedAmountFlow()
    val totalLitersFlow: Flow<Double?> = saleDao.getTotalLitersDistributedFlow()

    suspend fun getInventoryForDate(dateStr: String): MilkInventoryEntity? {
        return milkInventoryDao.getInventoryForDate(dateStr)
    }

    suspend fun insertOrUpdateInventory(cow: Double, buffalo: Double, a2: Double, dateStr: String) {
        val inventory = MilkInventoryEntity(
            dateStr = dateStr,
            cowLiters = cow,
            buffaloLiters = buffalo,
            a2Liters = a2,
            updatedAt = System.currentTimeMillis()
        )
        milkInventoryDao.insertInventory(inventory)
    }

    suspend fun getAllCustomers(): List<CustomerEntity> {
        return customerDao.getAllCustomers()
    }

    suspend fun insertCustomer(name: String, phone: String?, qrPreference: String) {
        val customer = CustomerEntity(
            name = name,
            phone = phone,
            qrPreference = qrPreference,
            isSynced = false,
            updatedAt = System.currentTimeMillis()
        )
        customerDao.insertCustomer(customer)
    }

    suspend fun deleteCustomer(id: String) {
        customerDao.deleteCustomer(id)
    }

    suspend fun insertSale(
        customerId: String,
        customerName: String,
        milkType: String,
        liters: Double,
        ratePerLiter: Double,
        paymentStatus: String,
        paymentType: String,
        location: String?
    ) {
        val sale = SaleEntity(
            id = UUID.randomUUID().toString(),
            customerId = customerId,
            customerName = customerName,
            milkType = milkType,
            liters = liters,
            ratePerLiter = ratePerLiter,
            totalAmount = liters * ratePerLiter,
            paymentStatus = paymentStatus,
            paymentType = paymentType,
            location = location,
            createdAt = System.currentTimeMillis(),
            isSynced = false,
            updatedAt = System.currentTimeMillis()
        )
        saleDao.insertSale(sale)
    }

    suspend fun deleteSale(id: String) {
        saleDao.deleteSale(id)
    }

    suspend fun markSaleAsPaid(id: String, paymentType: String) {
        saleDao.updatePaymentStatus(
            id = id,
            status = "PAID",
            paymentType = paymentType,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun updateMilkPrice(milkType: String, newPrice: Double) {
        val currentPrices = pricesFlow.firstOrNull() ?: emptyList()
        val currentPriceEntity = currentPrices.find { it.milkType == milkType }
        val oldPrice = currentPriceEntity?.currentPrice ?: 0.0

        if (oldPrice != newPrice) {
            priceDao.insertPriceConfig(
                PriceConfigEntity(
                    milkType = milkType,
                    currentPrice = newPrice,
                    updatedAt = System.currentTimeMillis()
                )
            )
            priceDao.insertPriceLog(
                PriceLogEntity(
                    milkType = milkType,
                    oldPrice = oldPrice,
                    newPrice = newPrice,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun getUnsyncedSales(): List<SaleEntity> {
        return saleDao.getUnsyncedSales()
    }

    suspend fun markSaleSynced(id: String) {
        saleDao.markSaleSynced(id, System.currentTimeMillis())
    }
}
