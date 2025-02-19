package com.example.autoclean.data.model.model

data class CreateAccountDto (
    val name: String,
    val email: String,
    val password: String,
    val role: String
)