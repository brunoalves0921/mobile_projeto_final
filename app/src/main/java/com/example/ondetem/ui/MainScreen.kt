package com.example.ondetem.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ondetem.data.Produto
import com.example.ondetem.ui.components.TopBar
import com.example.ondetem.ui.screens.AjudaScreen
import com.example.ondetem.ui.screens.CadastroScreen
import com.example.ondetem.ui.screens.ConfiguracoesScreen
import com.example.ondetem.ui.screens.DetalhesScreen
import com.example.ondetem.ui.screens.FavoritosScreen
import com.example.ondetem.ui.screens.HomeScreen
import com.example.ondetem.ui.screens.LoginScreen
import com.example.ondetem.ui.screens.RoleSelectionScreen
import com.example.ondetem.ui.screens.VendedorHomeScreen
import com.example.ondetem.viewmodel.ProdutoViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen(
    viewModel: ProdutoViewModel,
    darkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    favoriteProducts: List<Produto>,
    onToggleFavorite: (Produto) -> Unit,
    areNotificationsEnabled: Boolean,
    onToggleNotifications: () -> Unit
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
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
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
                FavoritosScreen(favoriteProducts = favoriteProducts) { id ->
                    navController.navigate("detalhes/$id")
                    currentRoute.value = "detalhes"
                }
            }
            composable("config") {
                currentRoute.value = "config"
                ConfiguracoesScreen(
                    viewModel = viewModel,
                    darkMode = darkMode,
                    onToggleDarkMode = onToggleDarkMode,
                    areNotificationsEnabled = areNotificationsEnabled,
                    onToggleNotifications = onToggleNotifications
                )
            }
            composable("ajuda") {
                currentRoute.value = "ajuda"
                AjudaScreen()
            }
            composable("detalhes/{id}") { backStackEntry ->
                currentRoute.value = "detalhes"
                val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: return@composable
                DetalhesScreen(
                    produtoId = id,
                    favoriteProducts = favoriteProducts,
                    onToggleFavorite = onToggleFavorite,
                    areNotificationsEnabled = areNotificationsEnabled
                )
            }
            composable("selecionar_perfil") {
                currentRoute.value = "selecionar_perfil"
                RoleSelectionScreen(
                    onSelecionarCliente = {
                        navController.navigate("home")
                        currentRoute.value = "home"
                    },
                    onSelecionarVendedor = {
                        navController.navigate("login")
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