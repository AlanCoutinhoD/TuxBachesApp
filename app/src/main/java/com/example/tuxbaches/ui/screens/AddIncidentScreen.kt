package com.example.tuxbaches.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tuxbaches.R
import com.example.tuxbaches.ui.viewmodel.IncidentViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import coil.compose.rememberAsyncImagePainter
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIncidentScreen(
    viewModel: IncidentViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val state = viewModel.state
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf("high") }
    var location by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
        // Remove the createNewFile() call as createTempFile already creates the file
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (!success) {
            imageUri = null
        }
    }

    val cameraPermissions = arrayOf(
        Manifest.permission.CAMERA
        // Remove WRITE_EXTERNAL_STORAGE as it's not needed for API 29+ and causes issues
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            val file = createImageFile()
            imageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            takePictureLauncher.launch(imageUri)
        }
    }

    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        imageUri = uri
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onNavigateToHome()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            try {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { location_ ->
                        location = location_?.let { Pair(it.latitude, it.longitude) }
                    }
            } catch (e: SecurityException) {
                // Handle error
            }
        }
    }

    LaunchedEffect(Unit) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                try {
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { location_ ->
                            location = location_?.let { Pair(it.latitude, it.longitude) }
                        }
                } catch (e: SecurityException) {
                    // Handle error
                }
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Añadir Incidente") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Regresar"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = when(severity) {
                        "high" -> "Alta"
                        "medium" -> "Media"
                        else -> "Baja"
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Severidad") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Alta") },
                        onClick = {
                            severity = "high"
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Media") },
                        onClick = {
                            severity = "medium"
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Baja") },
                        onClick = {
                            severity = "low"
                            expanded = false
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        try {
                            if (cameraPermissions.all {
                                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                            }) {
                                val file = createImageFile()
                                imageUri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    file
                                )
                                takePictureLauncher.launch(imageUri)
                            } else {
                                cameraPermissionLauncher.launch(cameraPermissions)
                            }
                        } catch (e: Exception) {
                            // Log or handle the exception
                            println("Camera error: ${e.message}")
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Tomar Foto")
                }

                Button(
                    onClick = {
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Galería")
                }
            }

            imageUri?.let { uri ->
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Imagen seleccionada",
                    modifier = Modifier.size(100.dp)
                )
            }

            if (location != null) {
                Text("Ubicación obtenida: ${location?.first}, ${location?.second}")
            } else {
                Text("Obteniendo ubicación...", color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    location?.let { (latitude, longitude) ->
                        viewModel.createIncident(
                            "pothole",
                            title,
                            latitude,
                            longitude,
                            severity,
                            imageUri
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = location != null && 
                         title.isNotBlank() && 
                         severity.isNotBlank()
            ) {
                Text("Guardar Incidente")
            }

            if (state.error != null) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}