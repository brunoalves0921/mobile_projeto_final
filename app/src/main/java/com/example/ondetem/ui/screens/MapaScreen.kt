package com.example.ondetem.ui.screens

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ondetem.data.LojaRepository
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapaScreen() {
    // --- TODA A SUA LÓGICA DE DADOS E ESTADO PERMANECE IGUAL ---
    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )
    val todasAsLojas by LojaRepository.getTodasAsLojasFlow().collectAsStateWithLifecycle(initialValue = null)
    var searchQuery by remember { mutableStateOf("") }

    val lojasFiltradas by remember(searchQuery, todasAsLojas) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                todasAsLojas
            } else {
                todasAsLojas?.filter { loja ->
                    loja.nome.contains(searchQuery, ignoreCase = true)
                }
            }
        }
    }

    val quixada = LatLng(-4.9705, -39.0158)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(quixada, 13f)
    }

    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    // ================================================================
    // ===== NOVO LAYOUT COM CARD APLICADO AQUI =======================
    // ================================================================

    // Adicionamos um padding geral na tela para o Card não ficar colado nas bordas.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // O Card agora é o contêiner principal para a busca e o mapa.
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(16.dp), // Cantos arredondados
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Sombra sutil
        ) {
            // A Column interna organiza a busca em cima e o mapa embaixo.
            Column(modifier = Modifier.fillMaxSize()) {
                // O seu campo de busca, agora dentro do Card.
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar loja no mapa...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp), // Padding interno do Card
                    singleLine = true
                )

                // O Box com o mapa ocupa o resto do espaço do Card.
                Box(modifier = Modifier.weight(1f)) {
                    val mapProperties = MapProperties(
                        isMyLocationEnabled = locationPermissionState.status.isGranted
                    )
                    // O seu GoogleMap, com toda a lógica de marcadores, intacta.
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = mapProperties
                    ) {
                        lojasFiltradas?.forEach { loja ->
                            if (loja.latitude != 0.0 && loja.longitude != 0.0) {
                                MarkerInfoWindow(
                                    state = MarkerState(position = LatLng(loja.latitude, loja.longitude)),
                                    title = loja.nome,
                                    snippet = loja.endereco,
                                    content = {
                                        Column(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surface)
                                                .padding(12.dp)
                                        ) {
                                            Text(loja.nome, fontWeight = FontWeight.Bold)
                                            Text(loja.endereco)
                                        }
                                    }
                                )
                            }
                        }
                    }
                    // O seu indicador de loading, agora dentro do Box do mapa no Card.
                    if (todasAsLojas == null) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}