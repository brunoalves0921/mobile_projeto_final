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
import com.example.ondetem.data.Loja
import com.example.ondetem.data.LojaRepository
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapaScreen() {
    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )
    // Renomeado para clareza
    val todasAsLojas by LojaRepository.getTodasAsLojasFlow().collectAsStateWithLifecycle(initialValue = null)

    // --- MUDANÇA 1: ESTADOS PARA A BUSCA E FILTRO ---
    var searchQuery by remember { mutableStateOf("") }

    // Cria uma lista filtrada que se recalcula automaticamente quando a busca ou a lista de lojas muda.
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

    // Posição inicial da câmera (Quixadá, CE)
    val quixada = LatLng(-4.9705, -39.0158)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(quixada, 13f)
    }

    // Efeito para pedir permissão de localização
    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    // --- MUDANÇA 2: ALTERAR O LAYOUT DE BOX PARA COLUMN ---
    Column(modifier = Modifier.fillMaxSize()) {
        // --- MUDANÇA 3: ADICIONAR O CAMPO DE BUSCA ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Buscar loja no mapa...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            singleLine = true
        )

        // O mapa agora fica dentro da Column
        Box(modifier = Modifier.fillMaxSize()) {
            val mapProperties = MapProperties(
                isMyLocationEnabled = locationPermissionState.status.isGranted
            )
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties
            ) {
                // --- MUDANÇA 4: USAR A LISTA FILTRADA PARA EXIBIR OS MARCADORES ---
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
            // Indicador de loading inicial
            if (todasAsLojas == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}