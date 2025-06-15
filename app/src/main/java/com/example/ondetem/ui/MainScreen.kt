package com.example.ondetem.ui

import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
// Imports explícitos do Material 3 para evitar conflitos
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
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
    unreadNotificationCount: Int,
    onClearFavorites: () -> Unit
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

    val topBarTitle by derivedStateOf {
        when {
            currentRoute?.startsWith("detalhes/") == true -> {
                val produtoId = navBackStackEntry?.arguments?.getString("id")
                viewModel.todosOsProdutos.value.find { it.id == produtoId }?.nome ?: "Detalhes"
            }
            currentRoute == "home" -> "Onde Tem?"
            currentRoute == "favoritos" -> "Favoritos"
            currentRoute == "mapa" -> "Mapa de Lojas"
            currentRoute == "notificacoes" -> "Meus Alertas"
            currentRoute == "config" -> "Configurações"
            currentRoute == "ajuda" -> "Ajuda"
            currentRoute == "perfil_vendedor" -> "Meu Perfil"
            currentRoute?.startsWith("detalhes_loja/") == true -> "Detalhes da Loja"
            currentRoute?.startsWith("editar_produto/") == true -> "Editar Produto"
            currentRoute?.startsWith("cadastro_produto/") == true -> "Novo Produto"
            currentRoute?.startsWith("editar_loja/") == true -> "Editar Loja"
            currentRoute?.startsWith("cadastro_loja/") == true -> "Nova Loja"
            else -> currentRoute?.replaceFirstChar { it.titlecase() } ?: ""
        }
    }

    val navItems = listOf(
        NavItem("home", "Home", Icons.Default.Home),
        NavItem("mapa", "Mapa", Icons.Default.Map),
        NavItem("favoritos", "Favoritos", Icons.Default.Favorite),
        NavItem("notificacoes", "Alertas", Icons.Default.Notifications)
    )
    val bottomNavRoutes = navItems.map { it.route }

    val showBottomBar = currentRoute in bottomNavRoutes
    var menuExpanded by remember { mutableStateOf(false) }

    val screensWithoutMenu = listOf("login", "cadastro", "selecionar_perfil")

    Scaffold(
        topBar = {
            if (currentRoute !in screensWithoutMenu) {
                TopBar(
                    title = topBarTitle,
                    canNavigateBack = navController.previousBackStackEntry != null && currentRoute !in bottomNavRoutes,
                    onNavigateBack = { navController.popBackStack() },
                    actions = {
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
                )
            }
        },
        // ==================================================================
        // ===== ESTRUTURA COMPLETAMENTE NOVA E CORRETA =====================
        // ==================================================================
        bottomBar = {
            AnimatedVisibility(visible = showBottomBar) {
                BoxWithConstraints {
                    val itemWidth = this.maxWidth / navItems.size
                    val selectedIndex = remember(currentRoute) {
                        navItems.indexOfFirst { it.route == currentRoute }.takeIf { it != -1 } ?: 0
                    }
                    val indicatorXOffset by animateDpAsState(
                        targetValue = itemWidth * selectedIndex,
                        animationSpec = tween(durationMillis = 250),
                        label = "indicatorOffset"
                    )

                    val navigationBarColor = if (darkMode) {
                        com.example.ondetem.ui.theme.DarkSurface
                    } else {
                        com.example.ondetem.ui.theme.LightBackground
                    }

                    Column {
                        // A ÁREA DO INDICADOR
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .background(navigationBarColor)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(itemWidth * 0.6f)
                                    .fillMaxHeight()
                                    .offset(x = indicatorXOffset + (itemWidth * 0.2f))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }

                        // A BARRA DE NAVEGAÇÃO CUSTOMIZADA
                        Surface(
                            color = navigationBarColor,
                            tonalElevation = 3.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                navItems.forEach { item ->
                                    val isSelected = currentRoute == item.route
                                    val contentColor = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }

                                    // ======================================================================
                                    // ===== MUDANÇA PARA REMOVER O EFEITO DE CLIQUE ========================
                                    // ======================================================================
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            // Adicionamos parâmetros ao clickable
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null, // Esta linha desativa o efeito visual (ripple)
                                                onClick = {
                                                    navController.navigate(item.route) {
                                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                            )
                                            .padding(vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        // O conteúdo (ícone e texto) continua o mesmo
                                        if (item.route == "notificacoes" && unreadNotificationCount > 0) {
                                            BadgedBox(
                                                badge = { Badge { Text("$unreadNotificationCount") } }
                                            ) {
                                                Icon(
                                                    imageVector = item.icon,
                                                    contentDescription = item.label,
                                                    tint = contentColor
                                                )
                                            }
                                        } else {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.label,
                                                tint = contentColor
                                            )
                                        }
                                        Text(
                                            text = item.label,
                                            color = contentColor,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "selecionar_perfil",
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(animationSpec = tween(300)) }, exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) }, popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            // ... suas rotas ...
            composable("home") { HomeScreen(viewModel) { id -> navController.navigate("detalhes/$id") } }
            composable("favoritos") { FavoritosScreen(favoriteProducts) { id -> navController.navigate("detalhes/$id") } }
            composable("mapa") { MapaScreen() }
            composable("notificacoes") { NotificacoesScreen() }
            composable("config") { ConfiguracoesScreen(viewModel, darkMode, onToggleDarkMode, areNotificationsEnabled, onToggleNotifications, onClearFavorites) }
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