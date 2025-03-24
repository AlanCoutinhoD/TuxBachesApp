package com.example.tuxbaches.data.model

data class Incident(
    val id: Int,
    val user_id: Int,
    val type: String,
    val title: String,
    val description: String,
    val latitude: String,
    val longitude: String,
    val severity: String,
    val status: String,
    val image_url: String?,
    val created_at: String,
    val updated_at: String,
    val distance: Double  // Changed from Int to Double to match API response
)