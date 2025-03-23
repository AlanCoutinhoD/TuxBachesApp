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
            is AuthEvent.Register -> {
                register(event.username, event.email, event.password)
            }
            is AuthEvent.Login -> {
                login(event.email, event.password)
            }
        }
    }

    private fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            val result = repository.register(User(username, email, password))
            state = state.copy(
                isLoading = false,
                isAuthenticated = result.isSuccess,
                error = result.exceptionOrNull()?.message
            )
        }
    }

    private fun login(email: String, password: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            val result = repository.login(email, password)
            state = state.copy(
                isLoading = false,
                isAuthenticated = result.isSuccess,
                error = result.exceptionOrNull()?.message
            )
        }
    }
}

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null
)

sealed class AuthEvent {
    data class Register(val username: String, val email: String, val password: String) : AuthEvent()
    data class Login(val email: String, val password: String) : AuthEvent()
}