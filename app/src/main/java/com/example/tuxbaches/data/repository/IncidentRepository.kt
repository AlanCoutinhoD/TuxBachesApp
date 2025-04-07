package com.example.tuxbaches.data.repository

import android.app.Application
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.tuxbaches.data.api.IncidentApi
import com.example.tuxbaches.data.model.Incident
import com.example.tuxbaches.util.PreferencesKeys
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncidentRepository @Inject constructor(
    private val api: IncidentApi,
    private val dataStore: DataStore<Preferences>,
    private val application: Application
) {
    suspend fun createIncident(
        type: String,
        title: String,
        latitude: Double,
        longitude: Double,
        severity: String,
        imageUri: Uri?
    ): Incident {
        val token = dataStore.data.first()[PreferencesKeys.TOKEN] ?: throw Exception("No token found")
        
        val typeBody = type.toRequestBody("text/plain".toMediaTypeOrNull())
        val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val latBody = latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val lonBody = longitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val severityBody = severity.toRequestBody("text/plain".toMediaTypeOrNull())
        
        val imagePart = imageUri?.let { uri ->
            val inputStream = application.contentResolver.openInputStream(uri)
            val file = File.createTempFile("image", ".jpg").apply {
                inputStream?.use { it.copyTo(outputStream()) }
            }
            
            MultipartBody.Part.createFormData(
                "image",
                file.name,
                file.asRequestBody("image/*".toMediaTypeOrNull())
            )
        }

        return api.createIncident(
            "Bearer $token",
            typeBody,
            titleBody,
            latBody,
            lonBody,
            severityBody,
            imagePart
        )
    }

    suspend fun getNearbyIncidents(latitude: Double, longitude: Double): List<Incident> {
        val token = dataStore.data.first()[PreferencesKeys.TOKEN] ?: throw Exception("No token found")
        return api.getNearbyIncidents("Bearer $token", latitude, longitude)
    }
}