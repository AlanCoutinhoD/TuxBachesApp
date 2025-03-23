package com.example.tuxbaches.data.model

data class Incident(
    val type: String = "pothole",
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val severity: String
)