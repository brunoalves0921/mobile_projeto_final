package com.example.ondetem.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        // Chave para salvar os IDs dos favoritos
        val FAVORITES_KEY = stringSetPreferencesKey("favorite_product_ids")
    }

    // --- Dark Mode ---
    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }

    suspend fun saveDarkMode(isDarkMode: Boolean) {
        context.dataStore.edit { settings ->
            settings[DARK_MODE_KEY] = isDarkMode
        }
    }

    // --- Favoritos ---
    // Fluxo para ler os IDs dos favoritos salvos
    val favoriteProductIds: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[FAVORITES_KEY] ?: emptySet()
        }

    // Função para salvar os IDs dos favoritos
    suspend fun toggleFavorite(productId: String) {
        context.dataStore.edit { settings ->
            val currentFavorites = settings[FAVORITES_KEY] ?: emptySet()
            val newFavorites = currentFavorites.toMutableSet()
            if (newFavorites.contains(productId)) {
                newFavorites.remove(productId)
            } else {
                newFavorites.add(productId)
            }
            settings[FAVORITES_KEY] = newFavorites
        }
    }
}