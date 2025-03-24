package com.example.tuxbaches.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.tuxbaches.data.api.IncidentApi
import com.example.tuxbaches.data.model.Incident
import com.example.tuxbaches.util.PreferencesKeys
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncidentRepository @Inject constructor(
    private val api: IncidentApi,
    private val dataStore: DataStore<Preferences>
) {
    suspend fun createIncident(incident: Incident): Incident {
        val token = dataStore.data.first()[PreferencesKeys.TOKEN] ?: throw Exception("No token found")
        println("Creating incident with token: $token")
        val response = api.createIncident("Bearer $token", incident)
        println("API Response for create incident: $response")
        return response
    }

    suspend fun getNearbyIncidents(latitude: Double, longitude: Double): List<Incident> {
        try {
            println("Starting getNearbyIncidents - Lat: $latitude, Lon: $longitude")
            
            val token = dataStore.data.first()[PreferencesKeys.TOKEN]
            println("Token retrieved: ${token != null}")
            
            if (token == null) {
                println("Token is null - Authentication issue")
                throw Exception("No token found")
            }
            
            println("Making API call with token: ${token.take(10)}...")
            val response = api.getNearbyIncidents("Bearer $token", latitude, longitude)
            println("API Response received - Number of incidents: ${response.size}")
            println("First incident (if any): ${response.firstOrNull()}")
            
            return response
        } catch (e: Exception) {
            println("Error in getNearbyIncidents: ${e.javaClass.simpleName} - ${e.message}")
            throw e
        }
    }
}