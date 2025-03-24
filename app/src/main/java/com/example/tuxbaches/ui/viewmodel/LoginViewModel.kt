package com.example.tuxbaches.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.tuxbaches.data.repository.AuthRepository
import com.example.tuxbaches.data.model.LoginResponse
import com.example.tuxbaches.util.PreferencesKeys
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    var state by mutableStateOf(LoginState())
        private set

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true)
                val response = authRepository.login(email, password)
                println("Login response: $response") // Debug log
                
                dataStore.edit { preferences ->
                    preferences[PreferencesKeys.TOKEN] = response.token // Access token from LoginResponse
                }
                
                state = state.copy(isSuccess = true, isLoading = false)
            } catch (e: Exception) {
                println("Login error: ${e.message}") // Debug log
                state = state.copy(error = e.message ?: "Error desconocido", isLoading = false)
            }
        }
    }
}

data class LoginState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)