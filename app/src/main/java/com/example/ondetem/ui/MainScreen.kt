package com.example.ondetem.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
    val showBottomBar = currentRoute.value in listOf("home", "favoritos")
    val bottomItems = listOf("home", "favoritos")

    Scaffold(
        topBar = {
            TopBar(
                currentRoute = currentRoute.value,
                canNavigateBack = navController.previousBackStackEntry != null && currentRoute.value != "selecionar_perfil",
                onNavigateBack = { navController.popBackStack() },
                onNavigateTo = { navController.navigate(it); currentRoute.value = it }
            )
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(imageVector = when (screen) {"home" -> Icons.Default.Home else -> Icons.Default.Favorite}, contentDescription = null) },
                            label = { Text(screen.replaceFirstChar { it.uppercase() }) },
                            selected = currentRoute.value == screen,
                            onClick = { navController.navigate(screen); currentRoute.value = screen }
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
                    onLojaClick = { lojaId -> navController.navigate("detalhes_loja/$lojaId"); currentRoute.value = "detalhes_loja" }
                )
            }
            composable("cadastro_loja/{vendedorId}") { backStackEntry ->
                currentRoute.value = "cadastro_loja"
                val vendedorId = backStackEntry.arguments?.getString("vendedorId") ?: ""
                CadastroLojaScreen(vendedorId = vendedorId) { navController.popBackStack(); currentRoute.value = "perfil_vendedor" }
            }
            composable("detalhes_loja/{lojaId}") { backStackEntry ->
                currentRoute.value = "detalhes_loja"
                val lojaId = backStackEntry.arguments?.getString("lojaId") ?: ""
                DetalhesLojaScreen(lojaId = lojaId, onAddProduto = { navController.navigate("cadastro_produto/$lojaId"); currentRoute.value = "cadastro_produto" }, onEditProduto = { produtoId -> navController.navigate("editar_produto/$produtoId"); currentRoute.value = "editar_produto" })
            }
            composable("cadastro_produto/{lojaId}") { backStackEntry ->
                currentRoute.value = "cadastro_produto"
                val lojaId = backStackEntry.arguments?.getString("lojaId")
                CadastroProdutoScreen(lojaId = lojaId, produtoId = null, onProdutoSalvo = { navController.popBackStack(); currentRoute.value = "detalhes_loja" })
            }
            composable("editar_produto/{produtoId}") { backStackEntry ->
                currentRoute.value = "editar_produto"
                val produtoId = backStackEntry.arguments?.getString("produtoId")
                CadastroProdutoScreen(lojaId = null, produtoId = produtoId, onProdutoSalvo = { navController.popBackStack(); currentRoute.value = "detalhes_loja" })
            }
        }
    }
}