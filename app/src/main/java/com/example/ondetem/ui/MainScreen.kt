package com.example.ondetem.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ondetem.data.Produto
import com.example.ondetem.ui.components.TopBar
import com.example.ondetem.ui.screens.*
import com.example.ondetem.viewmodel.ProdutoViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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
    val currentRoute = remember { mutableStateOf("selecionar_perfil") }
    val showBottomBar = currentRoute.value in listOf("home", "favoritos")
    val bottomItems = listOf("home", "favoritos")

    Scaffold(
        topBar = {
            TopBar(
                currentRoute = currentRoute.value,
                canNavigateBack = navController.previousBackStackEntry != null && currentRoute.value != "selecionar_perfil",
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
                        navController.navigate("home") {
                            popUpTo("selecionar_perfil") { inclusive = true }
                        }
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
                    onLoginSucesso = { email ->
                        navController.navigate("perfil_vendedor/$email") {
                            popUpTo("selecionar_perfil") { inclusive = true }
                        }
                        currentRoute.value = "perfil_vendedor"
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

            // NOVA ROTA: Perfil do Vendedor
            composable("perfil_vendedor/{email}") { backStackEntry ->
                currentRoute.value = "perfil_vendedor"
                val email = backStackEntry.arguments?.getString("email") ?: ""
                PerfilVendedorScreen(
                    vendedorEmail = email,
                    onCadastrarLoja = {
                        navController.navigate("cadastro_loja/$email")
                        currentRoute.value = "cadastro_loja"
                    },
                    onLogout = {
                        navController.navigate("selecionar_perfil") {
                            // Limpa a pilha de navegação até o perfil, removendo-o
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                        // Força a navegação para o início
                        navController.navigate("selecionar_perfil")
                        currentRoute.value = "selecionar_perfil"
                    },
                    // Ação ao clicar na loja
                    onLojaClick = { nomeLoja ->
                        // Codifica o nome da loja para passar como argumento de URL
                        val encodedNomeLoja = URLEncoder.encode(nomeLoja, StandardCharsets.UTF_8.toString())
                        navController.navigate("detalhes_loja/$encodedNomeLoja")
                        currentRoute.value = "detalhes_loja"
                    }
                )
            }

            // NOVA ROTA: Cadastro de Loja
            composable("cadastro_loja/{vendedorEmail}") { backStackEntry ->
                currentRoute.value = "cadastro_loja"
                val email = backStackEntry.arguments?.getString("vendedorEmail") ?: ""
                CadastroLojaScreen(
                    vendedorEmail = email,
                    onLojaCadastrada = {
                        navController.popBackStack()
                        currentRoute.value = "perfil_vendedor"
                    }
                )
            }

            // NOVA ROTA: Detalhes da Loja
            composable("detalhes_loja/{nomeLoja}") { backStackEntry ->
                currentRoute.value = "detalhes_loja"
                val nomeLoja = backStackEntry.arguments?.getString("nomeLoja") ?: ""
                DetalhesLojaScreen(
                    nomeLoja = nomeLoja,
                    onAddProduto = {
                        val encodedNomeLoja = URLEncoder.encode(nomeLoja, StandardCharsets.UTF_8.toString())
                        navController.navigate("cadastro_produto/$encodedNomeLoja")
                        currentRoute.value = "cadastro_produto"
                    }
                )
            }

            // NOVA ROTA: Cadastro de Produto
            composable("cadastro_produto/{nomeLoja}") { backStackEntry ->
                currentRoute.value = "cadastro_produto"
                val nomeLoja = backStackEntry.arguments?.getString("nomeLoja") ?: ""
                CadastroProdutoScreen(
                    nomeLoja = nomeLoja,
                    onProdutoCadastrado = {
                        navController.popBackStack()
                        currentRoute.value = "detalhes_loja"
                    }
                )
            }
        }
    }
}