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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ondetem.data.Produto
import com.example.ondetem.ui.components.TopBar
import com.example.ondetem.ui.screens.*
import com.example.ondetem.viewmodel.ProdutoViewModel

// Classe para facilitar a criação dos itens de navegação
data class NavItem(val route: String, val label: String, val icon: ImageVector)

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
    // Observa a rota atual de forma mais robusta
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Itens da barra de navegação
    val navItems = listOf(
        NavItem("home", "Home", Icons.Default.Home),
        NavItem("mapa", "Mapa", Icons.Default.Map), // <-- NOVO ITEM
        NavItem("favoritos", "Favoritos", Icons.Default.Favorite)
    )
    val bottomNavRoutes = navItems.map { it.route }

    val showBottomBar = currentRoute in bottomNavRoutes
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (currentRoute != "selecionar_perfil") {
                TopBar(
                    currentRoute = currentRoute ?: "",
                    canNavigateBack = navController.previousBackStackEntry != null && currentRoute !in bottomNavRoutes,
                    onNavigateBack = { navController.popBackStack() },
                    actions = {
                        if (currentRoute in bottomNavRoutes) {
                            Box {
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                                }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Configurações") },
                                        onClick = {
                                            navController.navigate("config")
                                            menuExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Ajuda") },
                                        onClick = {
                                            navController.navigate("ajuda")
                                            menuExpanded = false
                                        }
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
                    navItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    // on the back stack as users select items
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination when
                                    // re-selecting the same item
                                    launchSingleTop = true
                                    // Restore state when re-selecting a previously selected item
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController, startDestination = "selecionar_perfil", modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(animationSpec = tween(300)) }, exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) }, popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            // Rotas existentes
            composable("home") { HomeScreen(viewModel) { id -> navController.navigate("detalhes/$id") } }
            composable("favoritos") { FavoritosScreen(favoriteProducts) { id -> navController.navigate("detalhes/$id") } }

            // --- NOVA ROTA PARA O MAPA ---
            composable("mapa") { MapaScreen() }

            composable("config") { ConfiguracoesScreen(viewModel, darkMode, onToggleDarkMode, areNotificationsEnabled, onToggleNotifications) }
            composable("ajuda") { AjudaScreen() }
            composable("detalhes/{id}") { backStackEntry -> val id = backStackEntry.arguments?.getString("id") ?: return@composable; DetalhesScreen(id, viewModel, favoriteProducts, onToggleFavorite, areNotificationsEnabled) }
            composable("selecionar_perfil") { RoleSelectionScreen({ navController.navigate("home") { popUpTo("selecionar_perfil") { inclusive = true } } }, { navController.navigate("login") }) }
            composable("login") { LoginScreen(onLoginSucesso = { navController.navigate("perfil_vendedor") { popUpTo("selecionar_perfil") { inclusive = true } } }, onIrParaCadastro = { navController.navigate("cadastro") }) }
            composable("cadastro") { CadastroScreen({ navController.navigate("login") { popUpTo("cadastro") { inclusive = true } } }, { navController.popBackStack() }) }

            composable("perfil_vendedor") {
                PerfilVendedorScreen(
                    onCadastrarLoja = { vendedorId -> navController.navigate("cadastro_loja/$vendedorId") },
                    onLogout = { navController.navigate("selecionar_perfil") { popUpTo(navController.graph.startDestinationId) { inclusive = true } } },
                    onLojaClick = { lojaId -> navController.navigate("detalhes_loja/$lojaId") },
                    onEditLoja = { lojaId -> navController.navigate("editar_loja/$lojaId") }
                )
            }

            composable("cadastro_loja/{vendedorId}") { backStackEntry ->
                val vendedorId = backStackEntry.arguments?.getString("vendedorId")
                CadastroLojaScreen(
                    vendedorId = vendedorId,
                    lojaId = null,
                    onLojaSalva = { navController.popBackStack() }
                )
            }

            composable("editar_loja/{lojaId}") { backStackEntry ->
                val lojaId = backStackEntry.arguments?.getString("lojaId")
                CadastroLojaScreen(
                    vendedorId = null,
                    lojaId = lojaId,
                    onLojaSalva = { navController.popBackStack() }
                )
            }

            composable("detalhes_loja/{lojaId}") { backStackEntry ->
                val lojaId = backStackEntry.arguments?.getString("lojaId") ?: ""
                DetalhesLojaScreen(
                    lojaId = lojaId,
                    onAddProduto = { navController.navigate("cadastro_produto/$lojaId") },
                    onEditProduto = { produtoId -> navController.navigate("editar_produto/$produtoId") }
                )
            }

            composable("cadastro_produto/{lojaId}") { backStackEntry ->
                val lojaId = backStackEntry.arguments?.getString("lojaId")
                CadastroProdutoScreen(
                    lojaId = lojaId,
                    produtoId = null,
                    onProdutoSalvo = { navController.popBackStack() }
                )
            }

            composable("editar_produto/{produtoId}") { backStackEntry ->
                val produtoId = backStackEntry.arguments?.getString("produtoId")
                CadastroProdutoScreen(
                    lojaId = null,
                    produtoId = produtoId,
                    onProdutoSalvo = { navController.popBackStack() }
                )
            }
        }
    }
}