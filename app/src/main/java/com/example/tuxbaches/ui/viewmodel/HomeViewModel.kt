package com.example.tuxbaches.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.tuxbaches.data.api.IncidentApi
import com.example.tuxbaches.data.model.Incident
// Change this line
import com.example.tuxbaches.util.PreferencesKeys
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val incidentApi: IncidentApi,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    
    private val _incidents = MutableStateFlow<List<Incident>>(emptyList())
    val incidents: StateFlow<List<Incident>> = _incidents.asStateFlow()

    fun fetchNearbyIncidents(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                val token = dataStore.data.first()[PreferencesKeys.TOKEN] ?: return@launch
                val nearbyIncidents = incidentApi.getNearbyIncidents(
                    "Bearer $token",
                    latitude,
                    longitude
                )
                _incidents.value = nearbyIncidents
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}