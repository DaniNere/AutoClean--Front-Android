package com.example.autoclean.data.model

data class UpdateAccountDto(
    val fullname: String? = null,
    val email: String? = null,
    val password: String? = null,
    val role: String? = null,
    val phone: String? = null,
    val photoUrl: String? = null,
    val cpfOrCnpj: String? = null,
    val birthDate: String? = null
)
