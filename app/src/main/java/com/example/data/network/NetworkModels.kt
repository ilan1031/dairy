package com.example.data.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val email: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val businessName: String,
    val ownerName: String,
    val mobileNumber: String,
    val emailAddress: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class ProfileDto(
    val businessName: String,
    val ownerName: String,
    val mobileNumber: String,
    val emailAddress: String,
    val signupTimestamp: Long,
    val isLightTheme: Boolean = true,
    val language: String = "en"
)

@JsonClass(generateAdapter = true)
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val active: Boolean
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    val success: Boolean,
    val error: String? = null,
    val profile: ProfileDto? = null,
    val user: UserDto? = null
)

@JsonClass(generateAdapter = true)
data class ErrorResponse(
    val success: Boolean,
    val error: String?
)

@JsonClass(generateAdapter = true)
data class SubscriptionStatusDto(
    val active: Boolean = true,
    val blocked: Boolean = false,
    val plan: String = "premium",
    val daysLeft: Int = 365,
    val paymentMessage: String = "",
    val salesLimit: Int? = null,
    val customerLimit: Int? = null,
    val canCreate: Boolean? = null,
    val canUpdate: Boolean? = null,
    val canDelete: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class WhoAmIResponse(
    val authenticated: Boolean,
    val email: String? = null,
    val userId: String? = null,
    val subscriptionStatus: SubscriptionStatusDto? = null
)

@JsonClass(generateAdapter = true)
data class CustomerDto(
    val id: String,
    val name: String,
    val phone: String? = null,
    val qrPreference: String = "UPI",
    val address: String? = null,
    val notes: String? = null
)

@JsonClass(generateAdapter = true)
data class SaleDto(
    val id: String,
    val customerId: String,
    val customerName: String,
    val milkType: String,
    val liters: Double,
    val ratePerLiter: Double,
    val totalAmount: Double,
    val paymentStatus: String,
    val paymentType: String,
    val location: String? = null
)

@JsonClass(generateAdapter = true)
data class PriceConfigDto(
    val milkType: String,
    val currentPrice: Double
)

@JsonClass(generateAdapter = true)
data class InventoryDto(
    val dateStr: String,
    val cowLiters: Double,
    val buffaloLiters: Double,
    val a2Liters: Double,
    val customStocksRaw: String = ""
)

@JsonClass(generateAdapter = true)
data class BootstrapData(
    val profile: ProfileDto? = null,
    val customers: List<CustomerDto>? = null,
    val sales: List<SaleDto>? = null,
    val priceConfigs: List<PriceConfigDto>? = null,
    val inventory: List<InventoryDto>? = null
)

@JsonClass(generateAdapter = true)
data class BootstrapResponse(
    val success: Boolean,
    val error: String? = null,
    val data: BootstrapData? = null
)

@JsonClass(generateAdapter = true)
data class CustomerSaveResponse(
    val success: Boolean,
    val error: String? = null,
    val data: CustomerDto? = null
)

@JsonClass(generateAdapter = true)
data class SaleSaveResponse(
    val success: Boolean,
    val error: String? = null,
    val data: SaleDto? = null
)

@JsonClass(generateAdapter = true)
data class GenericResponse(
    val success: Boolean,
    val error: String? = null
)
