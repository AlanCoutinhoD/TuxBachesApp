package com.example.tuxbaches.data.model

data class LoginResponse(
    val message: String,
    val token: String,
    val userId: Int
)