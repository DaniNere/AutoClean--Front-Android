package com.example.autoclean.data.model.api

import com.example.autoclean.data.model.dto.CreateAccountDto
import com.example.autoclean.data.model.dto.LoginDto
import com.example.autoclean.data.model.dto.UpdateAccountDto
import com.example.autoclean.data.model.dto.UpdateDocumentsDto
import com.example.autoclean.data.model.response.LoginResponse
import com.example.autoclean.data.model.response.UserResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @GET("auth/{uid}")
    fun checkUserExists(@Path("uid") uid: String): Call<UserResponse>

    @POST("auth/register")
    fun register(@Body createAccountDto: CreateAccountDto
    ):Call<Void>

    @POST("auth/login")
    suspend fun login(@Body loginDTO: LoginDto): Response<LoginResponse>

    @PATCH("/users/{id}/profile-picture")
    fun updateAccount(
        @Path("id") userId: String,
        @Body updateAccountDto: UpdateAccountDto
    ): Call<Void>

    @PATCH("/users/{id}/selfie-with-cnh")
    fun updateDocuments(
        @Path("id") userId: String,
        @Body updateDocumentsDto: UpdateDocumentsDto
    ): Call<Void>

    @PATCH("/users/{id}/cnh-photo")
    fun updateCnhPhoto(
        @Path("id") userId: String,
        @Body updateDocumentsDto: UpdateDocumentsDto
    ): Call<Void>


}

