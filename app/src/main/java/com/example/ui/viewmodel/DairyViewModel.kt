package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.data.database.AppDatabase
import com.example.data.entity.CustomerEntity
import com.example.data.entity.PriceConfigEntity
import com.example.data.entity.PriceLogEntity
import com.example.data.entity.SaleEntity
import com.example.data.repository.Repository
import com.example.worker.SyncWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class DairyViewModel(application: Application) : AndroidViewModel(application) {

    private val database: AppDatabase
    private val repository: Repository

    val customers: StateFlow<List<CustomerEntity>>
    val sales: StateFlow<List<SaleEntity>>
    val prices: StateFlow<List<PriceConfigEntity>>
    val priceLogs: StateFlow<List<PriceLogEntity>>

    val totalPending: StateFlow<Double>
    val totalCollected: StateFlow<Double>
    val totalLiters: StateFlow<Double>

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val sharedPrefs = application.getSharedPreferences("dairy_sync_prefs", android.content.Context.MODE_PRIVATE)

    private val _isLoggedIn = MutableStateFlow(sharedPrefs.getBoolean("is_logged_in", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _businessName = MutableStateFlow(sharedPrefs.getString("business_name", "Ganga Premium Dairy") ?: "Ganga Premium Dairy")
    val businessName: StateFlow<String> = _businessName.asStateFlow()

    private val _ownerName = MutableStateFlow(sharedPrefs.getString("owner_name", "Arun Kumar") ?: "Arun Kumar")
    val ownerName: StateFlow<String> = _ownerName.asStateFlow()

    private val _mobileNumber = MutableStateFlow(sharedPrefs.getString("mobile_number", "9876543210") ?: "9876543210")
    val mobileNumber: StateFlow<String> = _mobileNumber.asStateFlow()

    private val _emailAddress = MutableStateFlow(sharedPrefs.getString("email_address", "arun@gangadairy.com") ?: "arun@gangadairy.com")
    val emailAddress: StateFlow<String> = _emailAddress.asStateFlow()

    private val _password = MutableStateFlow(sharedPrefs.getString("password", "password") ?: "password")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isLightTheme = MutableStateFlow(sharedPrefs.getBoolean("is_light_theme", true)) // default to light theme (true)
    val isLightTheme: StateFlow<Boolean> = _isLightTheme.asStateFlow()

    fun setLightTheme(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("is_light_theme", enabled).apply()
        _isLightTheme.value = enabled
    }

    fun setLoggedIn(loggedIn: Boolean) {
        sharedPrefs.edit().putBoolean("is_logged_in", loggedIn).apply()
        _isLoggedIn.value = loggedIn
    }

    fun updateProfile(bName: String, oName: String, mobile: String, em: String, pass: String) {
        sharedPrefs.edit()
            .putString("business_name", bName)
            .putString("owner_name", oName)
            .putString("mobile_number", mobile)
            .putString("email_address", em)
            .putString("password", pass)
            .apply()
        _businessName.value = bName
        _ownerName.value = oName
        _mobileNumber.value = mobile
        _emailAddress.value = em
        _password.value = pass
    }

    init {
        // AppDatabase initialization
        database = AppDatabase.getDatabase(application, viewModelScope)
        repository = Repository(
            customerDao = database.customerDao(),
            saleDao = database.saleDao(),
            priceDao = database.priceDao()
        )

        // Expose flows with StateIn for lifecycle-aware compose collection
        customers = repository.customersFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        sales = repository.salesFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        prices = repository.pricesFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        priceLogs = repository.priceLogsFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        totalPending = repository.totalPendingFlow
            .map { it ?: 0.0 }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

        totalCollected = repository.totalCollectedFlow
            .map { it ?: 0.0 }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

        totalLiters = repository.totalLitersFlow
            .map { it ?: 0.0 }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    }

    fun addNewCustomer(name: String, phone: String?, qrPreference: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertCustomer(name, phone, qrPreference)
        }
    }

    fun deleteCustomer(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCustomer(id)
        }
    }

    fun getCurrentLocationString(): String {
        try {
            val locationManager = getApplication<Application>().getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
            
            // Check permissions safely
            val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(
                getApplication(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            
            val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(
                getApplication(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            
            if (hasFine || hasCoarse) {
                val providers = locationManager.getProviders(true)
                var bestLocation: android.location.Location? = null
                for (provider in providers) {
                    val l = locationManager.getLastKnownLocation(provider) ?: continue
                    if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                        bestLocation = l
                    }
                }
                if (bestLocation != null) {
                    val lat = String.format(java.util.Locale.US, "%.5f", bestLocation.latitude)
                    val lng = String.format(java.util.Locale.US, "%.5f", bestLocation.longitude)
                    return "GPS: ($lat, $lng)"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // Realistic simulated fallback location coordinates if provider hasn't locked yet
        val randomLat = String.format(java.util.Locale.US, "28.61%03d", (300..500).random())
        val randomLng = String.format(java.util.Locale.US, "77.20%03d", (300..500).random())
        return "Simulated GPS: ($randomLat, $randomLng)"
    }

    fun addSale(
        customerId: String,
        customerName: String,
        milkType: String,
        liters: Double,
        ratePerLiter: Double,
        paymentStatus: String,
        paymentType: String,
        location: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val resolvedLocation = location ?: getCurrentLocationString()
            repository.insertSale(
                customerId = customerId,
                customerName = customerName,
                milkType = milkType,
                liters = liters,
                ratePerLiter = ratePerLiter,
                paymentStatus = paymentStatus,
                paymentType = paymentType,
                location = resolvedLocation
            )
            // Trigger automatic WorkManager sync attempt
            scheduleWorkManagerSync()
        }
    }

    fun deleteSale(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSale(id)
        }
    }

    fun markAsPaid(id: String, paymentType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.markSaleAsPaid(id, paymentType)
            // Trigger background sync automatic check
            scheduleWorkManagerSync()
        }
    }

    fun updatePrice(milkType: String, newPrice: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMilkPrice(milkType, newPrice)
        }
    }

    /**
     * Manual synchronizer for instant visual feedback on the Live Ticker of Daily Pulse
     */
    fun triggerManualSync() {
        if (_isSyncing.value) return
        viewModelScope.launch(Dispatchers.IO) {
            _isSyncing.value = true
            try {
                val unsynced = repository.getUnsyncedSales()
                if (unsynced.isNotEmpty()) {
                    delay(2000) // Simulating network lag
                    unsynced.forEach { sale ->
                        repository.markSaleSynced(sale.id)
                    }
                } else {
                    delay(800) // standard responsive visual feedback
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSyncing.value = false
            }
        }
    }

    /**
     * Schedule standard WorkManager background worker sync
     */
    private fun scheduleWorkManagerSync() {
        val syncConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(syncConstraints)
            .build()

        WorkManager.getInstance(getApplication()).enqueueUniqueWork(
            "DataSyncQueue",
            ExistingWorkPolicy.KEEP,
            syncWorkRequest
        )
    }
}
