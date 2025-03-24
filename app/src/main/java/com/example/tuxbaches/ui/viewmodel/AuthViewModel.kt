package com.example.tuxbaches.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tuxbaches.data.model.User
import com.example.tuxbaches.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {
    var state by mutableStateOf(AuthState())
        private set

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.Register -> register(event.username, event.email, event.password)
            is AuthEvent.Login -> login(event.email, event.password)
        }
    }

    private fun login(email: String, password: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            try {
                val response = repository.login(email, password)
                // Save token after successful login
                repository.saveToken(response.token)
                state = state.copy(isAuthenticated = true, isLoading = false)
            } catch (e: Exception) {
                println("Login error: ${e.message}")
                state = state.copy(error = e.message ?: "Error desconocido", isLoading = false)
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            try {
                val user = User(
                    username = name,
                    email = email,
                    password = password
                )
                val result = repository.register(user)
                result.fold(
                    onSuccess = { 
                        state = state.copy(isSuccess = true, isLoading = false)
                    },
                    onFailure = { exception ->
                        state = state.copy(error = exception.message ?: "Error desconocido", isLoading = false)
                    }
                )
            } catch (e: Exception) {
                state = state.copy(error = e.message ?: "Error desconocido", isLoading = false)
            }
        }
    }
}

data class AuthState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null
)

sealed class AuthEvent {
    data class Register(val username: String, val email: String, val password: String) : AuthEvent()
    data class Login(val email: String, val password: String) : AuthEvent()
}