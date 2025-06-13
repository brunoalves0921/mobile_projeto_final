package com.example.ondetem.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
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

    val bottomNavRoutes = listOf("home", "favoritos")
    // A visibilidade da BottomBar não precisa mais do placeholder de menu
    val showBottomBar = currentRoute.value in bottomNavRoutes
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (currentRoute.value != "selecionar_perfil") {
                TopBar(
                    currentRoute = currentRoute.value,
                    canNavigateBack = navController.previousBackStackEntry != null && !bottomNavRoutes.contains(currentRoute.value),
                    onNavigateBack = { navController.popBackStack() },
                    // --- MUDANÇA 1: Adicionar o menu de três pontinhos como uma "ação" ---
                    actions = {
                        // O menu só aparecerá nas telas principais do cliente
                        if (currentRoute.value in bottomNavRoutes) {
                            Box {
                                // Ícone de três pontinhos que abre o menu
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                                }
                                // O menu suspenso (Dropdown)
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Configurações") },
                                        onClick = {
                                            navController.navigate("config")
                                            currentRoute.value = "config"
                                            menuExpanded = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.Settings, null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Ajuda") },
                                        onClick = {
                                            navController.navigate("ajuda")
                                            currentRoute.value = "ajuda"
                                            menuExpanded = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.Info, null) }
                                    )
                                }
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(visible = showBottomBar) {
                NavigationBar {
                    // Item Home (Permanece)
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, "Home") },
                        label = { Text("Home") },
                        selected = currentRoute.value == "home",
                        onClick = {
                            navController.navigate("home") { launchSingleTop = true; restoreState = true }
                            currentRoute.value = "home"
                        }
                    )

                    // Item Favoritos (Permanece)
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Favorite, "Favoritos") },
                        label = { Text("Favoritos") },
                        selected = currentRoute.value == "favoritos",
                        onClick = {
                            navController.navigate("favoritos") { launchSingleTop = true; restoreState = true }
                            currentRoute.value = "favoritos"
                        }
                    )

                    // --- MUDANÇA 2: REMOVER O ITEM DE MENU DAQUI ---
                    // O bloco NavigationBarItem que continha o DropdownMenu foi removido.
                }
            }
        }
    ) { padding ->
        // O NavHost continua igual, sem alterações necessárias.
        NavHost(
            navController = navController, startDestination = "selecionar_perfil", modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(animationSpec = tween(300)) }, exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) }, popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            composable("home") { currentRoute.value = "home"; HomeScreen(viewModel) { id -> navController.navigate("detalhes/$id"); currentRoute.value = "detalhes" } }
            composable("favoritos") { currentRoute.value = "favoritos"; FavoritosScreen(favoriteProducts) { id -> navController.navigate("detalhes/$id"); currentRoute.value = "detalhes" } }
            composable("config") { currentRoute.value = "config"; ConfiguracoesScreen(viewModel, darkMode, onToggleDarkMode, areNotificationsEnabled, onToggleNotifications) }
            composable("ajuda") { currentRoute.value = "ajuda"; AjudaScreen() }
            composable("detalhes/{id}") { backStackEntry -> currentRoute.value = "detalhes"; val id = backStackEntry.arguments?.getString("id") ?: return@composable; DetalhesScreen(id, viewModel, favoriteProducts, onToggleFavorite, areNotificationsEnabled) }
            composable("selecionar_perfil") { currentRoute.value = "selecionar_perfil"; RoleSelectionScreen({ navController.navigate("home") { popUpTo("selecionar_perfil") { inclusive = true } }; currentRoute.value = "home" }, { navController.navigate("login"); currentRoute.value = "login" }) }
            composable("login") { currentRoute.value = "login"; LoginScreen(onLoginSucesso = { navController.navigate("perfil_vendedor") { popUpTo("selecionar_perfil") { inclusive = true } }; currentRoute.value = "perfil_vendedor" }, onIrParaCadastro = { navController.navigate("cadastro"); currentRoute.value = "cadastro" }) }
            composable("cadastro") { currentRoute.value = "cadastro"; CadastroScreen({ navController.navigate("login") { popUpTo("cadastro") { inclusive = true } }; currentRoute.value = "login" }, { navController.popBackStack(); currentRoute.value = "login" }) }

            composable("perfil_vendedor") {
                currentRoute.value = "perfil_vendedor"
                PerfilVendedorScreen(
                    onCadastrarLoja = { vendedorId -> navController.navigate("cadastro_loja/$vendedorId"); currentRoute.value = "cadastro_loja" },
                    onLogout = { navController.navigate("selecionar_perfil") { popUpTo(navController.graph.startDestinationId) { inclusive = true } }; navController.navigate("selecionar_perfil"); currentRoute.value = "selecionar_perfil" },
                    onLojaClick = { lojaId -> navController.navigate("detalhes_loja/$lojaId"); currentRoute.value = "detalhes_loja" },
                    onEditLoja = { lojaId -> navController.navigate("editar_loja/$lojaId"); currentRoute.value = "editar_loja"}
                )
            }

            composable("cadastro_loja/{vendedorId}") { backStackEntry ->
                currentRoute.value = "cadastro_loja"
                val vendedorId = backStackEntry.arguments?.getString("vendedorId")
                CadastroLojaScreen(
                    vendedorId = vendedorId,
                    lojaId = null,
                    onLojaSalva = { navController.popBackStack(); currentRoute.value = "perfil_vendedor" }
                )
            }

            composable("editar_loja/{lojaId}") { backStackEntry ->
                currentRoute.value = "editar_loja"
                val lojaId = backStackEntry.arguments?.getString("lojaId")
                CadastroLojaScreen(
                    vendedorId = null,
                    lojaId = lojaId,
                    onLojaSalva = { navController.popBackStack(); currentRoute.value = "perfil_vendedor" }
                )
            }

            composable("detalhes_loja/{lojaId}") { backStackEntry ->
                currentRoute.value = "detalhes_loja"
                val lojaId = backStackEntry.arguments?.getString("lojaId") ?: ""
                DetalhesLojaScreen(
                    lojaId = lojaId,
                    onAddProduto = { navController.navigate("cadastro_produto/$lojaId"); currentRoute.value = "cadastro_produto" },
                    onEditProduto = { produtoId -> navController.navigate("editar_produto/$produtoId"); currentRoute.value = "editar_produto" }
                )
            }

            composable("cadastro_produto/{lojaId}") { backStackEntry ->
                currentRoute.value = "cadastro_produto"
                val lojaId = backStackEntry.arguments?.getString("lojaId")
                CadastroProdutoScreen(
                    lojaId = lojaId,
                    produtoId = null,
                    onProdutoSalvo = { navController.popBackStack(); currentRoute.value = "detalhes_loja" }
                )
            }

            composable("editar_produto/{produtoId}") { backStackEntry ->
                currentRoute.value = "editar_produto"
                val produtoId = backStackEntry.arguments?.getString("produtoId")
                CadastroProdutoScreen(
                    lojaId = null,
                    produtoId = produtoId,
                    onProdutoSalvo = { navController.popBackStack(); currentRoute.value = "detalhes_loja" }
                )
            }
        }
    }
}