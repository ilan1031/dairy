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

    suspend fun insertOrUpdateInventory(cow: Double, buffalo: Double, a2: Double, dateStr: String, customStocksRaw: String = "") {
        val inventory = MilkInventoryEntity(
            dateStr = dateStr,
            cowLiters = cow,
            buffaloLiters = buffalo,
            a2Liters = a2,
            customStocksRaw = customStocksRaw,
            updatedAt = System.currentTimeMillis()
        )
        milkInventoryDao.insertInventory(inventory)
    }

    suspend fun getAllCustomers(): List<CustomerEntity> {
        return customerDao.getAllCustomers()
    }

    suspend fun insertCustomer(id: String, name: String, phone: String?, qrPreference: String) {
        val customer = CustomerEntity(
            id = id,
            name = name,
            phone = phone,
            qrPreference = qrPreference,
            isSynced = false,
            updatedAt = System.currentTimeMillis()
        )
        customerDao.insertCustomer(customer)
    }

    suspend fun saveCustomerDetails(id: String, name: String, phone: String?, qrPreference: String, address: String?, notes: String?) {
        val customer = CustomerEntity(
            id = id,
            name = name,
            phone = phone,
            qrPreference = qrPreference,
            address = address,
            notes = notes,
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

    suspend fun getUnsyncedCustomers(): List<CustomerEntity> {
        return customerDao.getUnsyncedCustomers()
    }

    suspend fun markCustomerSynced(id: String) {
        customerDao.markCustomerSynced(id, System.currentTimeMillis())
    }

    suspend fun syncUnsyncedData(context: android.content.Context): Boolean {
        if (!com.example.data.network.NetworkHelper.isInternetAvailable(context)) {
            android.util.Log.d("Repository", "No internet available for sync.")
            return false
        }
        val apiService = com.example.data.network.ApiClient.getApiService(context)
        return try {
            // 1. Sync customers
            val unsyncedCustomers = customerDao.getUnsyncedCustomers()
            android.util.Log.d("Repository", "Syncing ${unsyncedCustomers.size} customers...")
            for (customer in unsyncedCustomers) {
                val dto = com.example.data.network.CustomerDto(
                    id = customer.id,
                    name = customer.name,
                    phone = customer.phone,
                    qrPreference = customer.qrPreference,
                    address = customer.address,
                    notes = customer.notes
                )
                val response = apiService.saveCustomer(dto)
                if (response.isSuccessful && response.body()?.success == true) {
                    customerDao.markCustomerSynced(customer.id, System.currentTimeMillis())
                    android.util.Log.d("Repository", "Customer ${customer.id} synced successfully.")
                } else {
                    android.util.Log.e("Repository", "Failed to sync customer ${customer.id}: ${response.errorBody()?.string()}")
                }
            }

            // 2. Sync sales
            val unsyncedSales = saleDao.getUnsyncedSales()
            android.util.Log.d("Repository", "Syncing ${unsyncedSales.size} sales...")
            for (sale in unsyncedSales) {
                val dto = com.example.data.network.SaleDto(
                    id = sale.id,
                    customerId = sale.customerId,
                    customerName = sale.customerName,
                    milkType = sale.milkType,
                    liters = sale.liters,
                    ratePerLiter = sale.ratePerLiter,
                    totalAmount = sale.totalAmount,
                    paymentStatus = sale.paymentStatus,
                    paymentType = sale.paymentType,
                    location = sale.location
                )
                val response = apiService.saveSale(dto)
                if (response.isSuccessful && response.body()?.success == true) {
                    saleDao.markSaleSynced(sale.id, System.currentTimeMillis())
                    android.util.Log.d("Repository", "Sale ${sale.id} synced successfully.")
                } else {
                    android.util.Log.e("Repository", "Failed to sync sale ${sale.id}: ${response.errorBody()?.string()}")
                }
            }
            true
        } catch (e: Exception) {
            android.util.Log.e("Repository", "Error during synchronization: ${e.message}", e)
            false
        }
    }

    suspend fun bootstrapDataFromServer(context: android.content.Context): Boolean {
        if (!com.example.data.network.NetworkHelper.isInternetAvailable(context)) return false
        val apiService = com.example.data.network.ApiClient.getApiService(context)
        return try {
            val response = apiService.bootstrap()
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data ?: return false
                
                // 1. Customers
                data.customers?.forEach { cust ->
                    customerDao.insertCustomer(
                        CustomerEntity(
                            id = cust.id,
                            name = cust.name,
                            phone = cust.phone,
                            qrPreference = cust.qrPreference,
                            address = cust.address,
                            notes = cust.notes,
                            isSynced = true,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }

                // 2. Sales
                data.sales?.forEach { sale ->
                    saleDao.insertSale(
                        SaleEntity(
                            id = sale.id,
                            customerId = sale.customerId,
                            customerName = sale.customerName,
                            milkType = sale.milkType,
                            liters = sale.liters,
                            ratePerLiter = sale.ratePerLiter,
                            totalAmount = sale.totalAmount,
                            paymentStatus = sale.paymentStatus,
                            paymentType = sale.paymentType,
                            location = sale.location,
                            isSynced = true,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }

                // 3. Price configs
                data.priceConfigs?.forEach { price ->
                    priceDao.insertPriceConfig(
                        PriceConfigEntity(
                            milkType = price.milkType,
                            currentPrice = price.currentPrice,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }

                // 4. Inventory
                data.inventory?.forEach { inv ->
                    milkInventoryDao.insertInventory(
                        MilkInventoryEntity(
                            dateStr = inv.dateStr,
                            cowLiters = inv.cowLiters,
                            buffaloLiters = inv.buffaloLiters,
                            a2Liters = inv.a2Liters,
                            customStocksRaw = inv.customStocksRaw,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("Repository", "Failed to bootstrap data from server: ${e.message}", e)
            false
        }
    }

    suspend fun checkSubscriptionFromServer(context: android.content.Context): com.example.data.network.SubscriptionStatusDto? {
        if (!com.example.data.network.NetworkHelper.isInternetAvailable(context)) return null
        val apiService = com.example.data.network.ApiClient.getApiService(context)
        return try {
            val response = apiService.whoAmI()
            if (response.isSuccessful && response.body()?.authenticated == true) {
                val subStatus = response.body()?.subscriptionStatus
                if (subStatus != null) {
                    val prefs = context.getSharedPreferences("dairy_sync_prefs", android.content.Context.MODE_PRIVATE)
                    prefs.edit()
                        .putBoolean("sub_active", subStatus.active)
                        .putBoolean("sub_blocked", subStatus.blocked)
                        .putString("sub_plan", subStatus.plan)
                        .putInt("sub_days_left", subStatus.daysLeft)
                        .putString("sub_payment_msg", subStatus.paymentMessage)
                        .apply()
                }
                subStatus
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("Repository", "Failed to check subscription status: ${e.message}", e)
            null
        }
    }
}
