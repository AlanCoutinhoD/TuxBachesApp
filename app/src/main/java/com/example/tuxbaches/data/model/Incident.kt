package com.example.tuxbaches.data.model

data class Incident(
    val id: Int,
    val user_id: Int,
    val type: String,
    val title: String,
    val description: String,
    val latitude: String,  // Matches the API response type
    val longitude: String, // Matches the API response type
    val severity: String,
    val status: String,
    val image_url: String?,
    val created_at: String,
    val updated_at: String,
    val distance: Int
)