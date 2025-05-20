package com.example.ondetem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.ondetem.ui.MainScreen
import com.example.ondetem.ui.theme.AppTheme
import com.example.ondetem.viewmodel.ProdutoViewModel

class MainActivity : ComponentActivity() {
    private val viewModel = ProdutoViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var darkMode by rememberSaveable { mutableStateOf(false) }

            AppTheme(darkTheme = darkMode) {
                MainScreen(
                    viewModel = viewModel,
                    darkMode = darkMode,
                    onToggleDarkMode = { darkMode = !darkMode }
                )
            }
        }
    }
}
