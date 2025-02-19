package com.example.autoclean.data.api

import com.example.autoclean.data.model.api.ApiService
import com.example.autoclean.data.model.api.TwilioApiService

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    // Base URL do serviço local
    private const val LOCAL_BASE_URL = "http://10.0.2.2:3000/"

    // Configura o OkHttpClient com um logging interceptor
    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    // Retrofit para o serviço local
    private val localRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(LOCAL_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        localRetrofit.create(ApiService::class.java)
    }

    val twilioService: TwilioApiService by lazy {
        localRetrofit.create(TwilioApiService::class.java)
    }

}