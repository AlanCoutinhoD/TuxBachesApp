package com.example.tuxbaches.ui.viewmodel

import android.location.Location
import android.net.Uri
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

    fun createIncident(
        type: String,
        title: String,
        latitude: Double,
        longitude: Double,
        severity: String,
        imageUri: Uri?
    ) {
        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true)
                repository.createIncident(
                    type,
                    title,
                    latitude,
                    longitude,
                    severity,
                    imageUri
                )
                state = state.copy(isSuccess = true, isLoading = false)
            } catch (e: Exception) {
                state = state.copy(error = e.message ?: "Unknown error", isLoading = false)
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