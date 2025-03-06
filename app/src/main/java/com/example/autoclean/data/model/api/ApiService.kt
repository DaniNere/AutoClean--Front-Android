package com.example.autoclean.data.model.api

import com.example.autoclean.data.model.UpdateAccountDto
import com.example.autoclean.data.model.model.CreateAccountDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("auth/register")
    fun register(@Body createAccountDto: CreateAccountDto
    ):Call<Void>

    @PATCH("/users/{id}/profile-picture")
    fun updateAccount(
        @Path("id") userId: String,
        @Body updateAccountDto: UpdateAccountDto
    ): Call<Void>

}

