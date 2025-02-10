package com.example.autoclean.data.model.api

import com.example.autoclean.data.model.CreateAccountDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("auth/register")
    fun register(@Body createAccountDto: CreateAccountDto): Call<Void>
}