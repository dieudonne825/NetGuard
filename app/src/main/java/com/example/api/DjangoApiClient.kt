package com.example.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object DjangoApiClient {
    var baseUrl: String = "https://netguard-admin.example.com/"
        private set

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private var retrofitService: DjangoApiService? = null

    fun getService(customBaseUrl: String? = null): DjangoApiService {
        val cleanUrl = customBaseUrl?.trim()
        val targetUrl = if (!cleanUrl.isNullOrBlank()) {
            if (cleanUrl.endsWith("/")) cleanUrl else "$cleanUrl/"
        } else baseUrl

        if (retrofitService == null || targetUrl != baseUrl) {
            baseUrl = targetUrl
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            retrofitService = retrofit.create(DjangoApiService::class.java)
        }
        return retrofitService!!
    }

    private fun String.isNullBlinkOrEmpty(): Boolean = this.trim().isEmpty()
}
