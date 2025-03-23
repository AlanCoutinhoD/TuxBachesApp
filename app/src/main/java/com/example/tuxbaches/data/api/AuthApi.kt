package com.example.tuxbaches.data.api

import retrofit2.http.POST
import retrofit2.http.Body

interface AuthApi {
    @POST("api/users/register")
    suspend fun register(@Body user: com.example.tuxbaches.data.model.User): com.example.tuxbaches.data.model.AuthResponse

    @POST("api/users/login")
    suspend fun login(@Body loginRequest: com.example.tuxbaches.data.model.LoginRequest): com.example.tuxbaches.data.model.AuthResponse
}