package com.example.tuxbaches.ui.viewmodel

import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.tuxbaches.data.model.Incident
import com.example.tuxbaches.data.api.IncidentApi
import com.example.tuxbaches.data.repository.IncidentRepository
import com.example.tuxbaches.util.PreferencesKeys
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import com.example.tuxbaches.utils.VoiceAlertManager
import javax.inject.Inject

@HiltViewModel
class IncidentViewModel @Inject constructor(
    private val repository: IncidentRepository,
    private val incidentApi: IncidentApi,
    private val dataStore: DataStore<Preferences>,
    val voiceAlertManager: VoiceAlertManager
) : ViewModel() {
    var state by mutableStateOf(IncidentState())
        private set

    fun createIncident(title: String, description: String, severity: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true)
                println("Creating incident - Title: $title, Location: ($latitude, $longitude)")
                
                val incident = Incident(
                    id = 0,
                    user_id = 0,
                    type = "pothole",
                    title = title,
                    description = description,
                    latitude = latitude.toString(),
                    longitude = longitude.toString(),
                    severity = severity,
                    status = "active",
                    image_url = null,
                    created_at = "",
                    updated_at = "",
                    distance = 0.0  // Changed from 0 to 0.0 to match Double type
                )

                println("Sending incident to API...")
                val result = repository.createIncident(incident)
                println("Incident created successfully: ${result.id}")
                
                state = state.copy(isSuccess = true, isLoading = false)
            } catch (e: Exception) {
                println("Error creating incident: ${e.message}")
                state = state.copy(error = e.message ?: "Error desconocido", isLoading = false)
            }
        }
    }

    fun checkNearbyIncidents(currentLocation: Location) {
        viewModelScope.launch {
            try {
                val incidents = repository.getNearbyIncidents(currentLocation.latitude, currentLocation.longitude)
                incidents.forEach { incident ->
                    val incidentLocation = Location("incident").apply {
                        latitude = incident.latitude.toDoubleOrNull() ?: 0.0
                        longitude = incident.longitude.toDoubleOrNull() ?: 0.0
                    }
                    val distance = currentLocation.distanceTo(incidentLocation).toInt()
                    
                    if (distance < 200) {
                        voiceAlertManager.speakIncidentAlert(distance, incident.type)
                    }
                }
            } catch (e: Exception) {
                println("Error checking nearby incidents: ${e.message}")
            }
        }
    }
}

data class IncidentState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)