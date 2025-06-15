package com.example.ondetem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
// ADICIONE ESTE IMPORT
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.ondetem.data.Produto
import com.example.ondetem.data.SettingsDataStore
import com.example.ondetem.data.UserRepository
import com.example.ondetem.ui.MainScreen
import com.example.ondetem.ui.theme.OndeTEMTheme
import com.example.ondetem.viewmodel.ProdutoViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: ProdutoViewModel by lazy {
        ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application))
            .get(ProdutoViewModel::class.java)
    }
    private lateinit var settingsDataStore: SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ESTA É A LINHA QUE FAZ A MÁGICA
        enableEdgeToEdge()

        settingsDataStore = SettingsDataStore(this)

        // Observa mudanças no estado de autenticação (login/logout)
        lifecycleScope.launch {
            Firebase.auth.addAuthStateListener { auth ->
                // Recria a UI com o estado do usuário atual (logado ou nulo)
                setupUI(auth.currentUser?.uid)
            }
        }
    }

    private fun setupUI(userId: String?) {
        lifecycleScope.launch {
            // Cria um fluxo para a contagem de notificações não lidas.
            // Se não houver usuário, o fluxo emite 0.
            val unreadCountFlow = userId?.let {
                UserRepository.getUnreadNotificationsCountFlow(it)
            } ?: flowOf(0)

            // Combina todos os fluxos de dados necessários para a UI
            combine(
                settingsDataStore.isDarkMode,
                settingsDataStore.favoriteProductIds,
                settingsDataStore.areNotificationsEnabled,
                viewModel.todosOsProdutos,
                unreadCountFlow.distinctUntilChanged() // <-- NOVO FLUXO ADICIONADO
            ) { isDarkMode, favoriteIds, notificationsEnabled, allProducts, unreadCount ->

                val favoriteProducts = allProducts.filter { produto ->
                    favoriteIds.contains(produto.id)
                }

                MainScreenState(
                    isDarkMode = isDarkMode,
                    favoriteProducts = favoriteProducts,
                    areNotificationsEnabled = notificationsEnabled,
                    unreadNotificationCount = unreadCount,
                    onClearFavorites = {
                        lifecycleScope.launch {
                            settingsDataStore.clearFavorites()
                        }
                    }
                )

            }.collect { state ->
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
                            favoriteProducts = state.favoriteProducts,
                            onToggleFavorite = { produto ->
                                lifecycleScope.launch {
                                    settingsDataStore.toggleFavorite(produto.id)
                                }
                            },
                            areNotificationsEnabled = state.areNotificationsEnabled,
                            onToggleNotifications = {
                                lifecycleScope.launch {
                                    settingsDataStore.saveNotificationsPreference(!state.areNotificationsEnabled)
                                }
                            },
                            // Passa a contagem para a MainScreen
                            unreadNotificationCount = state.unreadNotificationCount,
                            onClearFavorites = state.onClearFavorites // <-- Adicione esta linha
                        )
                    }
                }
            }
        }
    }
}

private data class MainScreenState(
    val isDarkMode: Boolean,
    val favoriteProducts: List<Produto>,
    val areNotificationsEnabled: Boolean,
    val unreadNotificationCount: Int,
    val onClearFavorites: () -> Unit // <-- Adicione esta linha
)