package com.example.ondetem.ui.screens

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    // --- A LÓGICA DE DADOS PERMANECE A MESMA ---
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

    // --- O LAYOUT COM CARD PERMANECE O MESMO ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ================================================================
                // ===== CAMPO DE BUSCA MODERNIZADO ===============================
                // ================================================================
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar loja no mapa...") }, // Usamos placeholder em vez de label
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    // Ícone para limpar a busca, que só aparece quando há texto
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpar busca")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    singleLine = true,
                    // Deixa o campo com o formato de pílula
                    shape = CircleShape,
                    // Customiza as cores para um visual mais limpo
                    colors = OutlinedTextFieldDefaults.colors(
                        // Cor da borda quando o campo está selecionado
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        // Borda transparente quando o campo não está focado
                        unfocusedBorderColor = Color.Transparent,
                        // Cor de fundo sutil para destacar o campo do card
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // O Box com o mapa continua o mesmo
                Box(modifier = Modifier.weight(1f)) {
                    // ... (código do GoogleMap e do CircularProgressIndicator)
                    val mapProperties = MapProperties(
                        isMyLocationEnabled = locationPermissionState.status.isGranted
                    )
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
                    if (todasAsLojas == null) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}