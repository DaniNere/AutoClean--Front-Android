package com.example.autoclean.data.model.dto

data class CreateAccountDto(
    val fullname: String,
    val email: String,
    val password: String,
    val role: String,
    val uid: String ="",
    val phone: String ="",
    val photoUrl: String ="",
    val cpfOrCnpj: String ="",
    val birthDate: String =""
)