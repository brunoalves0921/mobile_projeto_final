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
    val showBottomBar = currentRoute.value in listOf("home", "favoritos")
    val bottomItems = listOf("home", "favoritos")

    Scaffold(
        topBar = {
            TopBar(
                currentRoute = currentRoute.value,
                canNavigateBack = currentRoute.value !in listOf("home", "favoritos", "selecionar_perfil", "login", "cadastro"),
                onNavigateBack = { navController.popBackStack() },
                onNavigateTo = {
                    navController.navigate(it)
                    currentRoute.value = it
                }
            )
        },
        bottomBar = {
            if (showBottomBar) {
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
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "selecionar_perfil",
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
            composable("selecionar_perfil") {
                currentRoute.value = "selecionar_perfil"
                RoleSelectionScreen(
                    onSelecionarCliente = {
                        navController.navigate("home")
                        currentRoute.value = "home"
                    },
                    onSelecionarVendedor = {
                        navController.navigate("login") // tela futura
                        currentRoute.value = "login"
                    }
                )
            }
            composable("login") {
                currentRoute.value = "login"
                LoginScreen(
                    onLoginSucesso = {
                        navController.navigate("vendedor_home") {
                            popUpTo("selecionar_perfil") { inclusive = true }
                        }
                        currentRoute.value = "vendedor_home"
                    },
                    onIrParaCadastro = {
                        navController.navigate("cadastro")
                        currentRoute.value = "cadastro"
                    }
                )
            }

            composable("cadastro") {
                currentRoute.value = "cadastro"
                CadastroScreen(
                    onCadastroSucesso = {
                        navController.navigate("login") { popUpTo("cadastro") { inclusive = true } }
                        currentRoute.value = "login"
                    },
                    onVoltarParaLogin = {
                        navController.popBackStack()
                        currentRoute.value = "login"
                    }
                )
            }
            composable("vendedor_home") {
                currentRoute.value = "vendedor_home"
                VendedorHomeScreen()
            }
        }
    }
}
