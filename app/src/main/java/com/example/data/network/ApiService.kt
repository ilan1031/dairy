package com.example.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/whoami")
    suspend fun whoAmI(@Body request: Map<String, String>): Response<WhoAmIResponse>

    @POST("auth/logout")
    suspend fun logout(@Body request: Map<String, String>): Response<GenericResponse>

    @POST("data/bootstrap")
    suspend fun bootstrap(@Body request: Map<String, String>): Response<BootstrapResponse>

    @POST("data/customers/save")
    suspend fun saveCustomer(@Body request: CustomerDto): Response<CustomerSaveResponse>

    @POST("data/customers/delete")
    suspend fun deleteCustomer(@Body request: Map<String, String>): Response<GenericResponse>

    @POST("data/sales/save")
    suspend fun saveSale(@Body request: SaleDto): Response<SaleSaveResponse>

    @POST("data/sales/delete")
    suspend fun deleteSale(@Body request: Map<String, String>): Response<GenericResponse>

    @POST("data/sales/mark-paid")
    suspend fun markSalePaid(@Body request: Map<String, String>): Response<GenericResponse>

    @POST("data/prices/save")
    suspend fun savePrice(@Body request: Map<String, Any>): Response<GenericResponse>

    @POST("data/inventory/save")
    suspend fun saveInventory(@Body request: InventoryDto): Response<GenericResponse>
}
