package com.example.tuxbaches.data.api

import com.example.tuxbaches.data.model.Incident
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface IncidentApi {
    @POST("api/incidents")
    suspend fun createIncident(
        @Header("Authorization") token: String,
        @Body incident: Incident
    ): Incident
}