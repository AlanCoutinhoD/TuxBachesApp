package com.example.tuxbaches.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tuxbaches.data.model.Incident
import com.example.tuxbaches.data.repository.IncidentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IncidentViewModel @Inject constructor(
    private val repository: IncidentRepository
) : ViewModel() {
    var state by mutableStateOf(IncidentState())
        private set

    fun createIncident(title: String, description: String, severity: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            val incident = Incident(
                title = title,
                description = description,
                severity = severity,
                latitude = latitude,
                longitude = longitude
            )
            val result = repository.createIncident(incident)
            state = state.copy(
                isLoading = false,
                isSuccess = result.isSuccess,
                error = result.exceptionOrNull()?.message
            )
        }
    }
}

data class IncidentState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)