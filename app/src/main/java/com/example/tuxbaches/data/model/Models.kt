package com.example.tuxbaches.data.model

data class User(
    val id: Int = 0,
    val username: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val message: String,
    val token: String,
    val userId: Int
)