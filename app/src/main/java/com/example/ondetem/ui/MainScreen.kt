package com.example.ondetem.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

    val bottomItems = listOf("home", "favoritos")

    Scaffold(
        topBar = {
            TopBar(
                currentRoute = currentRoute.value,
                canNavigateBack = currentRoute.value !in listOf("home", "favoritos"),
                onNavigateBack = { navController.popBackStack() },
                onNavigateTo = {
                    navController.navigate(it)
                    currentRoute.value = it
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
                                    else -> Icons.Default.Home
                                },
                                contentDescription = null
                            )
                        },
                        label = { Text(screen.replaceFirstChar { it.uppercase() }) },
                        selected = currentRoute.value == screen,
                        onClick = {
                            navController.navigate(screen)
                            currentRoute.value = screen
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") {
                currentRoute.value = "home"
                HomeScreen(viewModel) { id ->
                    navController.navigate("detalhes/$id")
                    currentRoute.value = "detalhes"
                }
            }
            composable("favoritos") {
                currentRoute.value = "favoritos"
                FavoritosScreen(viewModel) { id ->
                    navController.navigate("detalhes/$id")
                    currentRoute.value = "detalhes"
                }
            }
            composable("config") {
                currentRoute.value = "config"
                ConfiguracoesScreen(viewModel, darkMode, onToggleDarkMode)
            }
            composable("ajuda") {
                currentRoute.value = "ajuda"
                AjudaScreen()
            }
            composable("detalhes/{id}") { backStackEntry ->
                currentRoute.value = "detalhes"
                val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: return@composable
                DetalhesScreen(produtoId = id, viewModel = viewModel)
            }
        }
    }
}
