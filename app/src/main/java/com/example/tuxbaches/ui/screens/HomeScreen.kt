package com.example.tuxbaches.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

import com.example.tuxbaches.R
// Change this import
import com.example.tuxbaches.data.model.Incident

// To either:
// Remove duplicate imports and keep only:
// OR move your Incident.kt file to match the expected package
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tuxbaches.ui.viewmodel.HomeViewModel
import com.example.tuxbaches.ui.viewmodel.IncidentViewModel
import kotlinx.coroutines.delay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    incidentViewModel: IncidentViewModel = hiltViewModel(),
    onNavigateToAddIncident: () -> Unit
) {
    val context = LocalContext.current
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    val incidents = viewModel.incidents.collectAsState().value
    var nearestIncident by remember { mutableStateOf<Pair<Incident, Double>?>(null) }

    // Configurar OSMdroid
    Configuration.getInstance().load(context, context.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE))

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            try {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            currentLocation = GeoPoint(location.latitude, location.longitude)
                            viewModel.fetchNearbyIncidents(location.latitude, location.longitude)
                        }
                    }
            } catch (e: SecurityException) {
                // Handle error
            }
        }
    }

    LaunchedEffect(Unit) {
        // Change to use title instead of hardcoded message
        incidentViewModel.voiceAlertManager.speakIncidentAlert(0, "Bienvenido a TuxBaches")
        
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                try {
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { location ->
                            if (location != null) {
                                currentLocation = GeoPoint(location.latitude, location.longitude)
                                viewModel.fetchNearbyIncidents(location.latitude, location.longitude)
                            }
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
                title = { Text("TuxBaches") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddIncident,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_warning),
                    contentDescription = "Añadir incidente",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val mapView = remember {
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)
                }
            }

            AndroidView(
                factory = { mapView },
                update = { mapView ->
                    mapView.overlays.clear()
                    
                    // Añadir marcador de ubicación actual
                    currentLocation?.let { location ->
                        mapView.controller.setCenter(location)
                        val currentLocationMarker = Marker(mapView).apply {
                            position = location
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = "Mi ubicación"
                            icon = ContextCompat.getDrawable(context, R.drawable.ic_location)
                        }
                        mapView.overlays.add(currentLocationMarker)
                    }
                    
                    // Log received incidents
                    println("Received incidents: ${incidents.joinToString("\n") { 
                        "ID: ${it.id}, Title: ${it.title}, Location: (${it.latitude}, ${it.longitude}), Severity: ${it.severity}"
                    }}")

                    // Add incident markers
                    incidents.forEach { incident ->
                        try {
                            val incidentLocation = GeoPoint(
                                incident.latitude.toDoubleOrNull() ?: 0.0,
                                incident.longitude.toDoubleOrNull() ?: 0.0
                            )
                            
                            println("Adding marker at: ${incidentLocation.latitude}, ${incidentLocation.longitude}")
                            
                            val marker = Marker(mapView).apply {
                                position = incidentLocation
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = incident.title
                                snippet = "${incident.description}\nSeveridad: ${incident.severity}"
                                icon = when(incident.severity) {
                                    "high" -> ContextCompat.getDrawable(context, R.drawable.ic_high_severity)
                                    "medium" -> ContextCompat.getDrawable(context, R.drawable.ic_medium_severity)
                                    else -> ContextCompat.getDrawable(context, R.drawable.ic_low_severity)
                                }
                            }
                            mapView.overlays.add(marker)
                            mapView.invalidate()
                        } catch (e: Exception) {
                            println("Error adding marker: ${e.message}")
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            DisposableEffect(mapView) {
                onDispose {
                    mapView.onPause()
                    mapView.onDetach()
                }
            }

            // Nearest incident alert
            LaunchedEffect(incidents) {
                currentLocation?.let { location ->
                    val androidLocation = Location("").apply {
                        latitude = location.latitude
                        longitude = location.longitude
                    }
                    delay(500)
                    
                    val nearest = incidents.minByOrNull { it.distance }
                    
                    nearest?.let { incident ->
                        if (incident.distance < 500) {
                            nearestIncident = incident to incident.distance
                            incidentViewModel.voiceAlertManager.speakIncidentAlert(
                                incident.distance.toInt(), 
                                incident.title
                            )
                        } else {
                            nearestIncident = null
                        }
                    } ?: run {
                        nearestIncident = null
                    }
                }
            }

            nearestIncident?.let { (incident, distance) ->
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Incidente cercano",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = incident.title,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Aproximadamente a ${distance.toInt()} metros",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}