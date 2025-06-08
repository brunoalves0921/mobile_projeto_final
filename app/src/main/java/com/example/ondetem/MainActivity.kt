package com.example.ondetem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.example.ondetem.data.Produto
import com.example.ondetem.data.SettingsDataStore
import com.example.ondetem.data.produtosMockados
import com.example.ondetem.ui.MainScreen
import com.example.ondetem.ui.theme.OndeTEMTheme // Vamos usar este tema
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
            // Combine para reagir a mudanças no modo escuro e nos favoritos
            combine(
                settingsDataStore.isDarkMode,
                settingsDataStore.favoriteProductIds
            ) { isDarkMode, favoriteIds ->
                // Filtra a lista principal de produtos para obter os objetos Produto favoritos
                val favoriteProducts = produtosMockados.filter { produto ->
                    favoriteIds.contains(produto.id.toString())
                }
                // Retorna os dois valores para o `collect`
                isDarkMode to favoriteProducts
            }.collect { (isDarkMode, favoriteProducts) ->
                setContent {
                    // Lembre-se que apagamos o AppTheme.kt, agora usamos OndeTEMTheme
                    OndeTEMTheme(darkTheme = isDarkMode) {
                        MainScreen(
                            viewModel = viewModel,
                            darkMode = isDarkMode,
                            onToggleDarkMode = {
                                lifecycleScope.launch {
                                    settingsDataStore.saveDarkMode(!isDarkMode)
                                }
                            },
                            // Passando a lista de favoritos e a função para alterá-la
                            favoriteProducts = favoriteProducts,
                            onToggleFavorite = { produto ->
                                lifecycleScope.launch {
                                    // A nova função toggleFavorite no DataStore simplifica a lógica aqui
                                    settingsDataStore.toggleFavorite(produto.id.toString())
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}