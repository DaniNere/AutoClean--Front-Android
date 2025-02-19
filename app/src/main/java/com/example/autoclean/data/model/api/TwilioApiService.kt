package com.example.autoclean.data.model.api


import com.example.autoclean.data.model.request.VerifyCodeTwilio
import com.example.autoclean.data.model.request.VerifyRequestDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


interface TwilioApiService {
    @POST("two-factor-auth/send-code")
    fun sendPhoneVerification(@Body request: VerifyRequestDto): Call<Void>

    @POST("/two-factor-auth/verify-code")
    fun validateVerificationCode(@Body request: VerifyCodeTwilio): Call<Void>

}