package com.example.autoclean.data.model.dto

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

data class UpdateDocumentsDto(
    val selfieUrl: String? = null,
    val frontCnhUrl: String? = null,
    val backCnhUrl: String? = null,
    val crlvUrl: String? = null,
    val validationKitVideo: String? = null
)