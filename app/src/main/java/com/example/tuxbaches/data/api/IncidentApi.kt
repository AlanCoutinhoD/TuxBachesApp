package com.example.tuxbaches.data.api

import com.example.tuxbaches.data.model.Incident
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface IncidentApi {
    @POST("api/incidents")
    suspend fun createIncident(
        @Header("Authorization") token: String,
        @Body incident: Incident
    ): Incident

    @GET("api/incidents/nearby")
    suspend fun getNearbyIncidents(
        @Header("Authorization") token: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Int = 5
    ): List<Incident>
}