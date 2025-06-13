package com.example.ondetem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.ondetem.data.Produto
import com.example.ondetem.data.SettingsDataStore
import com.example.ondetem.ui.MainScreen
import com.example.ondetem.ui.theme.OndeTEMTheme
import com.example.ondetem.viewmodel.ProdutoViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: ProdutoViewModel by lazy {
        ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application))
            .get(ProdutoViewModel::class.java)
    }
    private lateinit var settingsDataStore: SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsDataStore = SettingsDataStore(this)

        lifecycleScope.launch {
            // ALTERAÇÃO 1: Adicionar o StateFlow `viewModel.todosOsProdutos` ao combine.
            // Agora o combine vai esperar que TODOS os fluxos emitam um valor inicial.
            combine(
                settingsDataStore.isDarkMode,
                settingsDataStore.favoriteProductIds,
                settingsDataStore.areNotificationsEnabled,
                viewModel.todosOsProdutos // <- O novo fluxo é adicionado aqui
            ) { isDarkMode, favoriteIds, notificationsEnabled, allProducts -> // <- Nova variável

                // ALTERAÇÃO 2: A lista `allProducts` agora vem diretamente do fluxo combinado.
                // Isso garante que ela não estará vazia quando este código for executado.
                val favoriteProducts = allProducts.filter { produto ->
                    favoriteIds.contains(produto.id)
                }

                // Usar uma data class para organizar os dados é uma boa prática
                MainScreenState(isDarkMode, favoriteProducts, notificationsEnabled)

            }.collect { state -> // O 'it' foi substituído por 'state' para mais clareza
                setContent {
                    OndeTEMTheme(darkTheme = state.isDarkMode) {
                        MainScreen(
                            viewModel = viewModel,
                            darkMode = state.isDarkMode,
                            onToggleDarkMode = {
                                lifecycleScope.launch {
                                    settingsDataStore.saveDarkMode(!state.isDarkMode)
                                }
                            },
                            favoriteProducts = state.favoriteProducts, // Usamos o valor do state
                            onToggleFavorite = { produto ->
                                lifecycleScope.launch {
                                    settingsDataStore.toggleFavorite(produto.id)
                                }
                            },
                            areNotificationsEnabled = state.notificationsEnabled, // Usamos o valor do state
                            onToggleNotifications = {
                                lifecycleScope.launch {
                                    settingsDataStore.saveNotificationsPreference(!state.notificationsEnabled)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// Data class para ajudar a organizar o estado combinado
private data class MainScreenState(
    val isDarkMode: Boolean,
    val favoriteProducts: List<Produto>,
    val notificationsEnabled: Boolean
)