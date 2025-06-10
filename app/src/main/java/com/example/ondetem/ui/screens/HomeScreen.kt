package com.example.ondetem.ui.screens

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ondetem.ui.components.ProdutoCard
import com.example.ondetem.viewmodel.ProdutoViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(viewModel: ProdutoViewModel, onItemClick: (String) -> Unit) {

    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )

    // Efeito que ordena os produtos assim que a permissão é concedida
    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            viewModel.ordenarEExibirPorProximidade()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = viewModel.busca,
            onValueChange = { viewModel.buscar(it) },
            label = { Text("Buscar produto...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { viewModel.buscar(viewModel.busca) })
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.isLoading) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                CircularProgressIndicator()
                Spacer(Modifier.height(8.dp))
                Text(viewModel.statusMessage)
            }
        } else {
            if (viewModel.produtos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (viewModel.busca.isNotBlank()) "Nenhum produto encontrado para '${viewModel.busca}'." else "Busque um produto ou permita a localização para ver os itens mais próximos.",
                            textAlign = TextAlign.Center
                        )
                        if (!locationPermissionState.status.isGranted) {
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { locationPermissionState.launchPermissionRequest() }) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Usar minha localização")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(
                        items = viewModel.produtos,
                        key = { produto -> produto.id } // Chave estável para performance
                    ) { produto ->
                        ProdutoCard(produto = produto) {
                            onItemClick(produto.id)
                        }
                    }
                }
            }
        }
    }
}