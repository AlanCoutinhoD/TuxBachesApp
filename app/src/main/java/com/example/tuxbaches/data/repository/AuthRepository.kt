package com.example.tuxbaches.data.repository

import javax.inject.Inject
import javax.inject.Singleton
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.tuxbaches.data.api.AuthApi
import com.example.tuxbaches.data.model.User
import com.example.tuxbaches.data.model.AuthResponse
import com.example.tuxbaches.data.model.LoginRequest
import com.example.tuxbaches.util.PreferencesKeys

@Singleton
class AuthRepository @Inject constructor(
    private val api: AuthApi,
    private val dataStore: DataStore<Preferences>
) {
    suspend fun register(user: User): Result<AuthResponse> {
        return try {
            val response = api.register(user)
            saveToken(response.token)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): AuthResponse {
        val loginRequest = LoginRequest(email = email, password = password)
        return api.login(loginRequest)
    }

    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.TOKEN] = token
        }
    }
}