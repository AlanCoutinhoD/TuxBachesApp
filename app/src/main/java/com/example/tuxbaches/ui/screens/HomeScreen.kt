package com.example.tuxbaches.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.tuxbaches.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddIncident: () -> Unit
) {
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
            Text(
                text = "Bienvenido a TuxBaches",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}