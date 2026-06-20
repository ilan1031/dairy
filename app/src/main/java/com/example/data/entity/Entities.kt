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
    val isSynced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
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
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "price_configs")
data class PriceConfigEntity(
    @PrimaryKey val milkType: String, // "Cow Milk", "Buffalo Milk", "A2 Milk"
    val currentPrice: Double,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "price_logs")
data class PriceLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val milkType: String,
    val oldPrice: Double,
    val newPrice: Double,
    val timestamp: Long = System.currentTimeMillis()
)
