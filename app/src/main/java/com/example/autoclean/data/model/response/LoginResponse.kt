package com.example.autoclean.data.model.response

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)

data class User(
    val id: Int,
    val fullname: String,
    val email: String,
    val profilePicture: String?,
    val phone: String?,
    val cpfOrCnpj: String?
)

data class UserResponse(
    val exists: Boolean
)