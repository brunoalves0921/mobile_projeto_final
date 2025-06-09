package com.example.ondetem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
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
            combine(
                settingsDataStore.isDarkMode,
                settingsDataStore.favoriteProductIds,
                settingsDataStore.areNotificationsEnabled
            ) { isDarkMode, favoriteIds, notificationsEnabled ->

                // CORREÇÃO: Filtra a partir da lista mestra `todosOsProdutos`,
                // garantindo que a tela de favoritos sempre funcione.
                val favoriteProducts = viewModel.todosOsProdutos.filter { produto ->
                    favoriteIds.contains(produto.id.toString())
                }

                Triple(isDarkMode, favoriteProducts, notificationsEnabled)
            }.collect { (isDarkMode, favoriteProducts, notificationsEnabled) ->
                setContent {
                    OndeTEMTheme(darkTheme = isDarkMode) {
                        MainScreen(
                            viewModel = viewModel,
                            darkMode = isDarkMode,
                            onToggleDarkMode = {
                                lifecycleScope.launch {
                                    settingsDataStore.saveDarkMode(!isDarkMode)
                                }
                            },
                            favoriteProducts = favoriteProducts,
                            onToggleFavorite = { produto ->
                                lifecycleScope.launch {
                                    settingsDataStore.toggleFavorite(produto.id.toString())
                                }
                            },
                            areNotificationsEnabled = notificationsEnabled,
                            onToggleNotifications = {
                                lifecycleScope.launch {
                                    settingsDataStore.saveNotificationsPreference(!notificationsEnabled)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}