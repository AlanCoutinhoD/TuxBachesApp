package com.example.tuxbaches.data.repository

import javax.inject.Inject
import javax.inject.Singleton
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.tuxbaches.data.api.AuthApi
import com.example.tuxbaches.data.model.User
import com.example.tuxbaches.data.model.LoginRequest
import com.example.tuxbaches.data.model.AuthResponse

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

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = api.login(LoginRequest(email, password))
            saveToken(response.token)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("auth_token")] = token
        }
    }
}