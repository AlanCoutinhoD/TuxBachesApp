package com.example.tuxbaches.data.api

import com.example.tuxbaches.data.model.Incident
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface IncidentApi {
    @Multipart
    @POST("api/incidents")
    suspend fun createIncident(
        @Header("Authorization") token: String,
        @Part("type") type: RequestBody,
        @Part("title") title: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("severity") severity: RequestBody,
        @Part image: MultipartBody.Part?
    ): Incident

    @GET("api/incidents/nearby")
    suspend fun getNearbyIncidents(
        @Header("Authorization") token: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Int = 500
    ): List<Incident>
}