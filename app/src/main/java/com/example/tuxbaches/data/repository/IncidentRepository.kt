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
        return api.createIncident("Bearer $token", incident)
    }

    suspend fun getNearbyIncidents(latitude: Double, longitude: Double): List<Incident> {
        val token = dataStore.data.first()[PreferencesKeys.TOKEN] ?: throw Exception("No token found")
        return api.getNearbyIncidents("Bearer $token", latitude, longitude)
    }
}