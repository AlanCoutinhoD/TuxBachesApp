package com.example.tuxbaches.model

data class Incident(
    val id: String,
    val title: String,
    val description: String,
    val latitude: String,
    val longitude: String,
    val severity: String,
    val type: String
)