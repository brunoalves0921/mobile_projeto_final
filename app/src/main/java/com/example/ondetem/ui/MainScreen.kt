package com.example.ondetem.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
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
    val showBottomBar = currentRoute.value in (bottomNavRoutes + listOf("menu_placeholder")) // Use a placeholder
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (currentRoute.value != "selecionar_perfil") {
                TopBar(
                    currentRoute = currentRoute.value,
                    canNavigateBack = navController.previousBackStackEntry != null && !bottomNavRoutes.contains(currentRoute.value),
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateTo = { }
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(visible = showBottomBar) {
                NavigationBar {
                    // Item Home (Padrão)
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, "Home") },
                        label = { Text("Home") },
                        selected = currentRoute.value == "home",
                        onClick = {
                            navController.navigate("home") { launchSingleTop = true; restoreState = true }
                            currentRoute.value = "home"
                        }
                    )

                    // Item Favoritos (Padrão)
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Favorite, "Favoritos") },
                        label = { Text("Favoritos") },
                        selected = currentRoute.value == "favoritos",
                        onClick = {
                            navController.navigate("favoritos") { launchSingleTop = true; restoreState = true }
                            currentRoute.value = "favoritos"
                        }
                    )

                    // AQUI ESTÁ A CORREÇÃO:
                    // A própria RowScope da NavigationBar é usada para criar um item personalizado
                    // que se comporta como os outros.
                    this.NavigationBarItem(
                        selected = false,
                        onClick = { menuExpanded = true },
                        icon = {
                            Box { // O Box agora está dentro do 'icon', que é um lugar seguro
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
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
                        },
                        label = { Text("Menu") }
                    )
                }
            }
        }
    ) { padding ->
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
                    onEditLoja = { lojaId -> navController.navigate("editar_loja/$lojaId"); currentRoute.value = "editar_loja"} // Navega para a nova rota de edição
                )
            }

            composable("cadastro_loja/{vendedorId}") { backStackEntry ->
                currentRoute.value = "cadastro_loja"
                val vendedorId = backStackEntry.arguments?.getString("vendedorId")
                CadastroLojaScreen(
                    vendedorId = vendedorId,
                    lojaId = null, // Modo de adição
                    onLojaSalva = { navController.popBackStack(); currentRoute.value = "perfil_vendedor" }
                )
            }

            // NOVA ROTA PARA EDITAR LOJA
            composable("editar_loja/{lojaId}") { backStackEntry ->
                currentRoute.value = "editar_loja"
                val lojaId = backStackEntry.arguments?.getString("lojaId")
                CadastroLojaScreen(
                    vendedorId = null, // Não precisa no modo de edição
                    lojaId = lojaId,     // Passa o ID da loja para editar
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