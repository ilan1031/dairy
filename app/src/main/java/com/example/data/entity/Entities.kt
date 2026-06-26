package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String? = null,
    val qrPreference: String = "UPI",
    val address: String? = null,
    val notes: String? = null,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
    val userName: String? = null
)

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val customerId: String,
    val customerName: String, // Cached to avoid complex joins for rapid rendering
    val milkType: String, // "Cow Milk", "Buffalo Milk", "A2 Milk"
    val liters: Double,
    val ratePerLiter: Double,
    val totalAmount: Double, // Calculated: liters * ratePerLiter
    val paymentStatus: String, // "PAID", "PENDING"
    val paymentType: String, // "CASH", "UPI", "NONE"
    val location: String? = "Simulated Location (GPS Locked)",
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
    val userName: String? = null
)

@Entity(tableName = "price_configs")
data class PriceConfigEntity(
    @PrimaryKey val milkType: String, // "Cow Milk", "Buffalo Milk", "A2 Milk"
    val currentPrice: Double,
    val isSynced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
    val userName: String? = null
)

@Entity(tableName = "price_logs")
data class PriceLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val milkType: String,
    val oldPrice: Double,
    val newPrice: Double,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "milk_inventory")
data class MilkInventoryEntity(
    @PrimaryKey val dateStr: String, // String representation e.g. "yyyy-MM-dd"
    val cowLiters: Double,
    val buffaloLiters: Double,
    val a2Liters: Double,
    val customStocksRaw: String = "", // e.g. "Goat Milk:50.0,Camel Milk:30.0"
    val isSynced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
    val userName: String? = null
)
