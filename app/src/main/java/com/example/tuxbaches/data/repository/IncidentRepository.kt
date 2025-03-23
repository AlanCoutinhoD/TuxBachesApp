package com.example.tuxbaches.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.tuxbaches.data.api.IncidentApi
import com.example.tuxbaches.data.model.Incident
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncidentRepository @Inject constructor(
    private val api: IncidentApi,
    private val dataStore: DataStore<Preferences>
) {
    suspend fun createIncident(incident: Incident): Result<Incident> {
        return try {
            val token = dataStore.data.first()[stringPreferencesKey("auth_token")] ?: ""
            val response = api.createIncident("Bearer $token", incident)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}