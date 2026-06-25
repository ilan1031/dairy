package com.example.data.network

import android.content.Context
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private var apiService: ApiService? = null
    private var cookieJar: PersistentCookieJar? = null

    fun getCookieJar(context: Context): PersistentCookieJar {
        return cookieJar ?: synchronized(this) {
            val jar = PersistentCookieJar(context.applicationContext)
            cookieJar = jar
            jar
        }
    }

    fun getApiService(context: Context): ApiService {
        return apiService ?: synchronized(this) {
            val jar = getCookieJar(context)
            
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .cookieJar(jar)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            var baseUrl = try {
                BuildConfig.BE_Url
            } catch (e: Throwable) {
                ""
            }

            if (baseUrl.isBlank()) {
                baseUrl = "https://dairy.abielan.in/api/"
            }
            if (!baseUrl.endsWith("/")) {
                baseUrl = "$baseUrl/"
            }

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            val service = retrofit.create(ApiService::class.java)
            apiService = service
            service
        }
    }
}
