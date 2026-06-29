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
import com.example.data.network.UserDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

private fun parseDateStr(dateStr: String): Long? {
    if (dateStr.isBlank()) return null
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.parse(dateStr)?.time
    } catch (e: Exception) {
        null
    }
}

class Repository(
    private val customerDao: CustomerDao,
    private val saleDao: SaleDao,
    private val priceDao: PriceDao,
    private val milkInventoryDao: MilkInventoryDao
) {
    private fun normalizeUserName(userName: String?): String? = userName?.trim()?.takeIf { it.isNotBlank() }

    val customersFlow: Flow<List<CustomerEntity>> = customerDao.getAllCustomersFlow()
    val salesFlow: Flow<List<SaleEntity>> = saleDao.getAllSalesFlow()
    val pricesFlow: Flow<List<PriceConfigEntity>> = priceDao.getAllPricesFlow()
    val priceLogsFlow: Flow<List<PriceLogEntity>> = priceDao.getAllPriceLogsFlow()
    val inventoryFlow: Flow<List<MilkInventoryEntity>> = milkInventoryDao.getAllInventoryFlow()

    private val _usersFlow = MutableStateFlow<List<UserDto>>(emptyList())
    val usersFlow: Flow<List<UserDto>> = _usersFlow

    val totalPendingFlow: Flow<Double?> = saleDao.getTotalPendingAmountFlow()
    val totalCollectedFlow: Flow<Double?> = saleDao.getTotalCollectedAmountFlow()
    val totalLitersFlow: Flow<Double?> = saleDao.getTotalLitersDistributedFlow()

    suspend fun getInventoryForDate(dateStr: String): MilkInventoryEntity? {
        return milkInventoryDao.getInventoryForDate(dateStr)
    }

    suspend fun insertOrUpdateInventory(cow: Double, buffalo: Double, a2: Double, dateStr: String, customStocksRaw: String = "", userName: String? = null) {
        val inventory = MilkInventoryEntity(
            dateStr = dateStr,
            cowLiters = cow,
            buffaloLiters = buffalo,
            a2Liters = a2,
            customStocksRaw = customStocksRaw,
            isSynced = false,
            updatedAt = System.currentTimeMillis(),
            userName = normalizeUserName(userName)
        )
        milkInventoryDao.insertInventory(inventory)
    }

    suspend fun getAllCustomers(): List<CustomerEntity> {
        return customerDao.getAllCustomers()
    }

    suspend fun insertCustomer(id: String, name: String, phone: String?, qrPreference: String, userName: String? = null) {
        val customer = CustomerEntity(
            id = id,
            name = name,
            phone = phone,
            qrPreference = qrPreference,
            isSynced = false,
            updatedAt = System.currentTimeMillis(),
            userName = normalizeUserName(userName)
        )
        customerDao.insertCustomer(customer)
    }

    suspend fun saveCustomerDetails(id: String, name: String, phone: String?, qrPreference: String, address: String?, notes: String?, userName: String? = null) {
        val customer = CustomerEntity(
            id = id,
            name = name,
            phone = phone,
            qrPreference = qrPreference,
            address = address,
            notes = notes,
            isSynced = false,
            isDeleted = false,
            updatedAt = System.currentTimeMillis(),
            userName = normalizeUserName(userName)
        )
        customerDao.insertCustomer(customer)
    }

    suspend fun deleteCustomer(id: String) {
        customerDao.softDeleteCustomer(id, System.currentTimeMillis())
    }

    suspend fun insertSale(
        customerId: String,
        customerName: String,
        milkType: String,
        liters: Double,
        ratePerLiter: Double,
        paymentStatus: String,
        paymentType: String,
        location: String?,
        userName: String? = null,
        createdAt: Long = System.currentTimeMillis()
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
            createdAt = createdAt,
            isSynced = false,
            updatedAt = System.currentTimeMillis(),
            userName = normalizeUserName(userName)
        )
        saleDao.insertSale(sale)
    }

    suspend fun deleteSale(id: String) {
        saleDao.softDeleteSale(id, System.currentTimeMillis())
    }

    suspend fun markSaleAsPaid(id: String, paymentType: String) {
        saleDao.updatePaymentStatus(
            id = id,
            status = "PAID",
            paymentType = paymentType,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun updateMilkPrice(milkType: String, newPrice: Double, userName: String? = null) {
        val currentPrices = pricesFlow.firstOrNull() ?: emptyList()
        val currentPriceEntity = currentPrices.find { it.milkType == milkType }
        val oldPrice = currentPriceEntity?.currentPrice ?: 0.0

        if (oldPrice != newPrice) {
            priceDao.insertPriceConfig(
                PriceConfigEntity(
                    milkType = milkType,
                    currentPrice = newPrice,
                    isSynced = false,
                    updatedAt = System.currentTimeMillis(),
                    userName = normalizeUserName(userName)
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
        saleDao.markSaleSynced(id)
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
                if (customer.isDeleted) {
                    val response = apiService.deleteCustomer(mapOf("id" to customer.id))
                    if (response.isSuccessful && response.body()?.success == true) {
                        customerDao.hardDeleteCustomer(customer.id)
                        android.util.Log.d("Repository", "Customer ${customer.id} deleted from server & locally.")
                    } else {
                        android.util.Log.e("Repository", "Failed to sync customer deletion ${customer.id}")
                    }
                } else {
                    val dto = com.example.data.network.CustomerDto(
                        id = customer.id,
                        name = customer.name,
                        phone = customer.phone,
                        qrPreference = customer.qrPreference,
                        address = customer.address,
                        notes = customer.notes,
                        updatedAt = customer.updatedAt,
                        userName = customer.userName
                    )
                    val response = apiService.saveCustomer(dto)
                    if (response.isSuccessful && response.body()?.success == true) {
                        customerDao.markCustomerSynced(customer.id, System.currentTimeMillis())
                        android.util.Log.d("Repository", "Customer ${customer.id} synced successfully.")
                    } else {
                        android.util.Log.e("Repository", "Failed to sync customer ${customer.id}: ${response.errorBody()?.string()}")
                    }
                }
            }

            // 2. Sync sales
            val unsyncedSales = saleDao.getUnsyncedSales()
            android.util.Log.d("Repository", "Syncing ${unsyncedSales.size} sales...")
            for (sale in unsyncedSales) {
                if (sale.isDeleted) {
                    val response = apiService.deleteSale(mapOf("id" to sale.id))
                    if (response.isSuccessful && response.body()?.success == true) {
                        saleDao.hardDeleteSale(sale.id)
                        android.util.Log.d("Repository", "Sale ${sale.id} deleted from server & locally.")
                    } else {
                        android.util.Log.e("Repository", "Failed to sync sale deletion ${sale.id}")
                    }
                } else {
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
                        location = sale.location,
                        createdAt = sale.createdAt,
                        updatedAt = sale.updatedAt,
                        userName = sale.userName
                    )
                    val response = apiService.saveSale(dto)
                    if (response.isSuccessful && response.body()?.success == true) {
                        saleDao.markSaleSynced(sale.id)
                        android.util.Log.d("Repository", "Sale ${sale.id} synced successfully.")
                    } else {
                        android.util.Log.e("Repository", "Failed to sync sale ${sale.id}: ${response.errorBody()?.string()}")
                    }
                }
            }

            // 3. Sync prices
            val unsyncedPrices = priceDao.getUnsyncedPrices()
            android.util.Log.d("Repository", "Syncing ${unsyncedPrices.size} prices...")
            for (price in unsyncedPrices) {
                val response = apiService.savePrice(mapOf(
                    "milkType" to price.milkType,
                    "newPrice" to price.currentPrice,
                    "updatedAt" to price.updatedAt,
                    "userName" to (price.userName ?: "")
                ))
                if (response.isSuccessful && response.body()?.success == true) {
                    priceDao.markPriceSynced(price.milkType, System.currentTimeMillis())
                    android.util.Log.d("Repository", "Price config for ${price.milkType} synced successfully.")
                } else {
                    android.util.Log.e("Repository", "Failed to sync price for ${price.milkType}")
                }
            }

            // 4. Sync inventory
            val unsyncedInventory = milkInventoryDao.getUnsyncedInventory()
            android.util.Log.d("Repository", "Syncing ${unsyncedInventory.size} inventory logs...")
            for (inventory in unsyncedInventory) {
                val dto = com.example.data.network.InventoryDto(
                    dateStr = inventory.dateStr,
                    date = parseDateStr(inventory.dateStr),
                    cowLiters = inventory.cowLiters,
                    buffaloLiters = inventory.buffaloLiters,
                    a2Liters = inventory.a2Liters,
                    customStocksRaw = inventory.customStocksRaw,
                    updatedAt = inventory.updatedAt,
                    userName = inventory.userName
                )
                val response = apiService.saveInventory(dto)
                if (response.isSuccessful && response.body()?.success == true) {
                    milkInventoryDao.markInventorySynced(inventory.dateStr, System.currentTimeMillis())
                    android.util.Log.d("Repository", "Inventory for ${inventory.dateStr} synced successfully.")
                } else {
                    android.util.Log.e("Repository", "Failed to sync inventory for ${inventory.dateStr}")
                }
            }
            true
        } catch (e: Exception) {
            android.util.Log.e("Repository", "Error during synchronization: ${e.message}", e)
            false
        }
    }

    suspend fun bootstrapDataFromServer(context: android.content.Context, selectedUserId: String? = null): Boolean {
        if (!com.example.data.network.NetworkHelper.isInternetAvailable(context)) {
            android.util.Log.e("Repository", "Bootstrap failed: Internet not available")
            return false
        }
        val apiService = com.example.data.network.ApiClient.getApiService(context)
        android.util.Log.d("Repository", "Starting bootstrapDataFromServer... selectedUserId: $selectedUserId")
        return try {
            val reqBody = if (selectedUserId != null) mapOf("selectedUserId" to selectedUserId) else emptyMap()
            val response = apiService.bootstrap(reqBody)
            android.util.Log.d("Repository", "Bootstrap API response code: ${response.code()}")
            if (response.isSuccessful) {
                val body = response.body()
                android.util.Log.d("Repository", "Bootstrap API success field: ${body?.success}")
                if (body?.success == true) {
                    val data = body.data
                    if (data == null) {
                        android.util.Log.e("Repository", "Bootstrap failed: data body is null")
                        return false
                    }

                    // Save Profile if returned
                    data.profile?.let { profile ->
                        val prefs = context.getSharedPreferences("dairy_sync_prefs", android.content.Context.MODE_PRIVATE)
                        prefs.edit()
                            .putString("business_name", profile.businessName)
                            .putString("owner_name", profile.ownerName)
                            .putString("mobile_number", profile.mobileNumber)
                            .putString("email_address", profile.emailAddress)
                            .apply()
                    }
                    // Save Branding if returned
                    data.brandingConfig?.let { branding ->
                        val prefs = context.getSharedPreferences("dairy_sync_prefs", android.content.Context.MODE_PRIVATE)
                        prefs.edit()
                            .putString("branding_system_name", branding.systemName)
                            .putString("branding_bank_name", branding.bankName)
                            .putString("branding_logo", branding.logo)
                            .putString("branding_address", branding.address)
                            .apply()
                    }

                    // Save users list from BE bootstrap
                    _usersFlow.value = data.users ?: emptyList()
                    
                    // 1. Customers
                    val customers = data.customers ?: emptyList()
                    android.util.Log.d("Repository", "Bootstrap: Received ${customers.size} customers")
                    customers.forEach { cust ->
                        val local = customerDao.getCustomerById(cust.id)
                        if (local == null || local.isSynced) {
                            customerDao.insertCustomer(
                                CustomerEntity(
                                    id = cust.id,
                                    name = cust.name,
                                    phone = cust.phone,
                                    qrPreference = cust.qrPreference,
                                    address = cust.address,
                                    notes = cust.notes,
                                    isSynced = true,
                                    isDeleted = false,
                                    updatedAt = cust.updatedAt ?: System.currentTimeMillis(),
                                    userName = normalizeUserName(cust.resolvedUserName)
                                )
                            )
                        }
                    }

                    // Delete local synced customers that are not present in server response (Disabled to preserve historical offline data)
                    /*
                    val localSyncedCustomers = customerDao.getSyncedCustomers()
                    val serverCustomerIds = customers.map { it.id }.toSet()
                    localSyncedCustomers.forEach { local ->
                        if (!serverCustomerIds.contains(local.id)) {
                            customerDao.hardDeleteCustomer(local.id)
                            android.util.Log.d("Repository", "Bootstrap cleanup: Deleted local synced customer ${local.id} (not in server)")
                        }
                    }
                    */

                    // 2. Sales
                    val sales = data.sales ?: emptyList()
                    android.util.Log.d("Repository", "Bootstrap: Received ${sales.size} sales")
                    sales.forEach { sale ->
                        val local = saleDao.getSaleById(sale.id)
                        if (local == null || local.isSynced) {
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
                                    createdAt = local?.createdAt ?: sale.createdAt ?: System.currentTimeMillis(),
                                    isSynced = true,
                                    isDeleted = false,
                                    updatedAt = sale.updatedAt ?: System.currentTimeMillis(),
                                    userName = normalizeUserName(local?.userName) ?: normalizeUserName(sale.resolvedUserName)
                                )
                            )
                        }
                    }

                    // Delete local synced sales that are not present in server response (Disabled to preserve historical offline data)
                    /*
                    val localSyncedSales = saleDao.getSyncedSales()
                    val serverSaleIds = sales.map { it.id }.toSet()
                    localSyncedSales.forEach { local ->
                        if (!serverSaleIds.contains(local.id)) {
                            saleDao.hardDeleteSale(local.id)
                            android.util.Log.d("Repository", "Bootstrap cleanup: Deleted local synced sale ${local.id} (not in server)")
                        }
                    }
                    */

                     // 3. Price configs
                     if (data.priceConfigs.isNullOrEmpty()) {
                         android.util.Log.d("Repository", "Bootstrap: No price configs in response, seeding default prices")
                         priceDao.insertPriceConfig(PriceConfigEntity("Cow Milk", 50.0, isSynced = true))
                         priceDao.insertPriceConfig(PriceConfigEntity("Buffalo Milk", 70.0, isSynced = true))
                         priceDao.insertPriceConfig(PriceConfigEntity("A2 Milk", 90.0, isSynced = true))
                     } else {
                         android.util.Log.d("Repository", "Bootstrap: Received ${data.priceConfigs.size} price configs")
                         data.priceConfigs.forEach { price ->
                             val local = priceDao.getPriceConfig(price.milkType)
                             if (local == null || local.isSynced) {
                                 priceDao.insertPriceConfig(
                                     PriceConfigEntity(
                                         milkType = price.milkType,
                                         currentPrice = price.currentPrice,
                                         isSynced = true,
                                         updatedAt = price.updatedAt ?: System.currentTimeMillis(),
                                         userName = normalizeUserName(price.resolvedUserName)
                                     )
                                 )
                             }
                         }

                         // Delete local synced price configs that are not present in server response
                         val localSyncedPrices = priceDao.getSyncedPriceConfigs()
                         val serverMilkTypes = data.priceConfigs.map { it.milkType }.toSet()
                         localSyncedPrices.forEach { local ->
                             if (!serverMilkTypes.contains(local.milkType)) {
                                 // priceDao.deletePriceConfig(local.milkType)
                                 // android.util.Log.d("Repository", "Bootstrap cleanup: Deleted local synced price config ${local.milkType} (not in server)")
                             }
                         }
                     }
     
                     // 4. Inventory
                     val inventory = data.inventory ?: emptyList()
                     android.util.Log.d("Repository", "Bootstrap: Received ${inventory.size} inventory items")
                     inventory.forEach { inv ->
                         val local = milkInventoryDao.getInventoryForDate(inv.dateStr)
                         if (local == null || local.isSynced) {
                             milkInventoryDao.insertInventory(
                                 MilkInventoryEntity(
                                     dateStr = inv.dateStr,
                                     cowLiters = inv.cowLiters,
                                     buffaloLiters = inv.buffaloLiters,
                                     a2Liters = inv.a2Liters,
                                     customStocksRaw = inv.customStocksRaw,
                                     isSynced = true,
                                     updatedAt = inv.updatedAt ?: System.currentTimeMillis(),
                                     userName = normalizeUserName(inv.resolvedUserName)
                                 )
                             )
                         }
                     }

                     // Delete local synced inventory items that are not present in server response
                     val localSyncedInventory = milkInventoryDao.getSyncedInventory()
                     val serverDates = inventory.map { it.dateStr }.toSet()
                     localSyncedInventory.forEach { local ->
                         if (!serverDates.contains(local.dateStr)) {
                             // milkInventoryDao.deleteInventory(local.dateStr)
                             // android.util.Log.d("Repository", "Bootstrap cleanup: Deleted local synced inventory ${local.dateStr} (not in server)")
                         }
                     }
                    android.util.Log.d("Repository", "Bootstrap data from server completed successfully!")
                    true
                } else {
                    android.util.Log.e("Repository", "Bootstrap failed: success is false. Error: ${body?.error}")
                    false
                }
            } else {
                android.util.Log.e("Repository", "Bootstrap failed with network error: ${response.errorBody()?.string()}")
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
            val response = apiService.whoAmI(emptyMap())
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
                        .putInt("sub_sales_limit", subStatus.salesLimit ?: -1)
                        .putInt("sub_customer_limit", subStatus.customerLimit ?: -1)
                        .putBoolean("sub_can_create", subStatus.canCreate ?: true)
                        .putBoolean("sub_can_update", subStatus.canUpdate ?: true)
                        .putBoolean("sub_can_delete", subStatus.canDelete ?: true)
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

    suspend fun saveProfileToServer(context: android.content.Context, profile: com.example.data.network.ProfileDto): com.example.data.network.ProfileDto? {
        if (!com.example.data.network.NetworkHelper.isInternetAvailable(context)) return null
        val apiService = com.example.data.network.ApiClient.getApiService(context)
        return try {
            val response = apiService.saveProfile(profile)
            if (response.isSuccessful && response.body()?.success == true) {
                val saved = response.body()?.data
                if (saved != null) {
                    val prefs = context.getSharedPreferences("dairy_sync_prefs", android.content.Context.MODE_PRIVATE)
                    prefs.edit()
                        .putString("business_name", saved.businessName)
                        .putString("owner_name", saved.ownerName)
                        .putString("mobile_number", saved.mobileNumber)
                        .putString("email_address", saved.emailAddress)
                        .putLong("signup_timestamp", saved.signupTimestamp)
                        .putBoolean("is_light_theme", saved.isLightTheme)
                        .putString("language", saved.language)
                        .apply()
                }
                saved
            } else null
        } catch (e: Exception) {
            android.util.Log.e("Repository", "Failed to save profile: ${e.message}", e)
            null
        }
    }

    suspend fun saveBrandingToServer(context: android.content.Context, branding: com.example.data.network.BrandingConfigDto): com.example.data.network.BrandingConfigDto? {
        if (!com.example.data.network.NetworkHelper.isInternetAvailable(context)) return null
        val apiService = com.example.data.network.ApiClient.getApiService(context)
        return try {
            val response = apiService.saveBranding(branding)
            if (response.isSuccessful && response.body()?.success == true) {
                val saved = response.body()?.data
                if (saved != null) {
                    val prefs = context.getSharedPreferences("dairy_sync_prefs", android.content.Context.MODE_PRIVATE)
                    prefs.edit()
                        .putString("branding_system_name", saved.systemName)
                        .putString("branding_bank_name", saved.bankName)
                        .putString("branding_logo", saved.logo)
                        .putString("branding_address", saved.address)
                        .apply()
                }
                saved
            } else null
        } catch (e: Exception) {
            android.util.Log.e("Repository", "Failed to save branding: ${e.message}", e)
            null
        }
    }

    suspend fun clearAllLocalData(context: android.content.Context, seedDefaults: Boolean = true) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val db = com.example.data.database.AppDatabase.getDatabase(context, kotlinx.coroutines.GlobalScope)
            db.clearAllTables()
            if (seedDefaults) {
                val priceDao = db.priceDao()
                priceDao.insertPriceConfig(PriceConfigEntity("Cow Milk", 50.0, isSynced = true))
                priceDao.insertPriceConfig(PriceConfigEntity("Buffalo Milk", 70.0, isSynced = true))
                priceDao.insertPriceConfig(PriceConfigEntity("A2 Milk", 90.0, isSynced = true))
            }
        }
    }
}
