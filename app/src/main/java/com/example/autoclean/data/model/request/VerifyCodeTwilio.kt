package com.example.autoclean.data.model.request

data class VerifyCodeTwilio(
    val phoneNumber: String,
    val code: String
)
