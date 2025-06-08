package com.example.ondetem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.example.ondetem.data.Produto
import com.example.ondetem.data.SettingsDataStore
import com.example.ondetem.data.produtosMockados
import com.example.ondetem.ui.MainScreen
import com.example.ondetem.ui.theme.OndeTEMTheme
import com.example.ondetem.viewmodel.ProdutoViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel = ProdutoViewModel()
    private lateinit var settingsDataStore: SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsDataStore = SettingsDataStore(this)

        lifecycleScope.launch {
            // Combine para ouvir os 3 fluxos: modo escuro, favoritos e notificações
            combine(
                settingsDataStore.isDarkMode,
                settingsDataStore.favoriteProductIds,
                settingsDataStore.areNotificationsEnabled
            ) { isDarkMode, favoriteIds, notificationsEnabled ->
                val favoriteProducts = produtosMockados.filter { produto ->
                    favoriteIds.contains(produto.id.toString())
                }
                // Retorna uma Tupla com os três valores
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
                            // ADICIONADO: Passando o estado e a função das notificações
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