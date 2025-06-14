package com.example.ondetem.ui

import android.Manifest
import android.os.Build
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ondetem.data.Produto
import com.example.ondetem.ui.components.TopBar
import com.example.ondetem.ui.screens.*
import com.example.ondetem.viewmodel.ProdutoViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

data class NavItem(val route: String, val label: String, val icon: ImageVector)

@OptIn(ExperimentalAnimationApi::class, ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ProdutoViewModel,
    darkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    favoriteProducts: List<Produto>,
    onToggleFavorite: (Produto) -> Unit,
    areNotificationsEnabled: Boolean,
    onToggleNotifications: () -> Unit,
    unreadNotificationCount: Int // <-- NOVO PARÂMETRO
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationPermissionState = rememberPermissionState(
            permission = Manifest.permission.POST_NOTIFICATIONS
        )
        LaunchedEffect(Unit) {
            if (!notificationPermissionState.status.isGranted) {
                notificationPermissionState.launchPermissionRequest()
            }
        }
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navItems = listOf(
        NavItem("home", "Home", Icons.Default.Home),
        NavItem("mapa", "Mapa", Icons.Default.Map),
        NavItem("favoritos", "Favoritos", Icons.Default.Favorite),
        NavItem("notificacoes", "Alertas", Icons.Default.Notifications)
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
                                    DropdownMenuItem(text = { Text("Configurações") }, onClick = { navController.navigate("config"); menuExpanded = false })
                                    DropdownMenuItem(text = { Text("Ajuda") }, onClick = { navController.navigate("ajuda"); menuExpanded = false })
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
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            label = { Text(item.label) },
                            icon = {
                                // --- MUDANÇA AQUI: LÓGICA DA BADGE ---
                                if (item.route == "notificacoes" && unreadNotificationCount > 0) {
                                    BadgedBox(
                                        badge = { Badge() } // Exibe uma bolinha vermelha
                                    ) {
                                        Icon(item.icon, contentDescription = item.label)
                                    }
                                } else {
                                    Icon(item.icon, contentDescription = item.label)
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
            composable("home") { HomeScreen(viewModel) { id -> navController.navigate("detalhes/$id") } }
            composable("favoritos") { FavoritosScreen(favoriteProducts) { id -> navController.navigate("detalhes/$id") } }
            composable("mapa") { MapaScreen() }

            // --- MUDANÇA 2: ADICIONAR A ROTA PARA A NOVA TELA ---
            composable("notificacoes") { NotificacoesScreen() }

            composable("config") { ConfiguracoesScreen(viewModel, darkMode, onToggleDarkMode, areNotificationsEnabled, onToggleNotifications) }
            composable("ajuda") { AjudaScreen() }
            composable("detalhes/{id}") { backStackEntry -> val id = backStackEntry.arguments?.getString("id") ?: return@composable; DetalhesScreen(id, viewModel, favoriteProducts, onToggleFavorite, areNotificationsEnabled) }
            composable("selecionar_perfil") { RoleSelectionScreen({ navController.navigate("home") { popUpTo("selecionar_perfil") { inclusive = true } } }, { navController.navigate("login") }) }
            composable("login") { LoginScreen(onLoginSucesso = { navController.navigate("perfil_vendedor") { popUpTo("selecionar_perfil") { inclusive = true } } }, onIrParaCadastro = { navController.navigate("cadastro") }) }
            composable("cadastro") { CadastroScreen({ navController.navigate("login") { popUpTo("cadastro") { inclusive = true } } }, { navController.popBackStack() }) }
            composable("perfil_vendedor") { PerfilVendedorScreen(onCadastrarLoja = { vendedorId -> navController.navigate("cadastro_loja/$vendedorId") }, onLogout = { navController.navigate("selecionar_perfil") { popUpTo(navController.graph.startDestinationId) { inclusive = true } } }, onLojaClick = { lojaId -> navController.navigate("detalhes_loja/$lojaId") }, onEditLoja = { lojaId -> navController.navigate("editar_loja/$lojaId") }) }
            composable("cadastro_loja/{vendedorId}") { backStackEntry -> val vendedorId = backStackEntry.arguments?.getString("vendedorId"); CadastroLojaScreen(vendedorId = vendedorId, lojaId = null, onLojaSalva = { navController.popBackStack() }) }
            composable("editar_loja/{lojaId}") { backStackEntry -> val lojaId = backStackEntry.arguments?.getString("lojaId"); CadastroLojaScreen(vendedorId = null, lojaId = lojaId, onLojaSalva = { navController.popBackStack() }) }
            composable("detalhes_loja/{lojaId}") { backStackEntry -> val lojaId = backStackEntry.arguments?.getString("lojaId") ?: ""; DetalhesLojaScreen(lojaId = lojaId, onAddProduto = { navController.navigate("cadastro_produto/$lojaId") }, onEditProduto = { produtoId -> navController.navigate("editar_produto/$produtoId") }) }
            composable("cadastro_produto/{lojaId}") { backStackEntry -> val lojaId = backStackEntry.arguments?.getString("lojaId"); CadastroProdutoScreen(lojaId = lojaId, produtoId = null, onProdutoSalvo = { navController.popBackStack() }) }
            composable("editar_produto/{produtoId}") { backStackEntry -> val produtoId = backStackEntry.arguments?.getString("produtoId"); CadastroProdutoScreen(lojaId = null, produtoId = produtoId, onProdutoSalvo = { navController.popBackStack() }) }
        }
    }
}