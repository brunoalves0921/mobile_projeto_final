package com.example.ondetem.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.ondetem.ui.components.TopBar
import com.example.ondetem.ui.screens.*
import com.example.ondetem.viewmodel.ProdutoViewModel

@Composable
fun MainScreen(
    viewModel: ProdutoViewModel,
    darkMode: Boolean,
    onToggleDarkMode: () -> Unit
) {
    val navController = rememberNavController()
    val currentRoute = remember { mutableStateOf("home") }

    val bottomItems = listOf("home", "favoritos", "config", "ajuda")

    Scaffold(
        topBar = {
            TopBar(
                onNavigateTo = { route ->
                    navController.navigate(route)
                    currentRoute.value = route
                }
            )
        },
        bottomBar = {
            NavigationBar {
                bottomItems.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = when (screen) {
                                    "home" -> Icons.Default.Home
                                    "favoritos" -> Icons.Default.Favorite
                                    "config" -> Icons.Default.Settings
                                    "ajuda" -> Icons.Default.Info
                                    else -> Icons.Default.Home
                                },
                                contentDescription = null
                            )
                        },
                        label = { Text(screen.replaceFirstChar { it.uppercase() }) },
                        selected = currentRoute.value == screen,
                        onClick = {
                            currentRoute.value = screen
                            navController.navigate(screen)
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                HomeScreen(viewModel) { id -> navController.navigate("detalhes/$id") }
            }
            composable("favoritos") {
                FavoritosScreen(viewModel) { id -> navController.navigate("detalhes/$id") }
            }
            composable("config") {
                ConfiguracoesScreen(viewModel, darkMode, onToggleDarkMode)
            }
            composable("ajuda") {
                AjudaScreen()
            }
            composable("detalhes/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: return@composable
                DetalhesScreen(produtoId = id, viewModel = viewModel)
            }
        }
    }
}
