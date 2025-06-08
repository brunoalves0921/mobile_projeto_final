package com.example.ondetem.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val FAVORITES_KEY = stringSetPreferencesKey("favorite_product_ids")
        // ADICIONADO: Chave para salvar a preferência de notificações
        val NOTIFICATIONS_KEY = booleanPreferencesKey("notifications_enabled")
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
    val favoriteProductIds: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[FAVORITES_KEY] ?: emptySet()
        }

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

    // --- Notificações ---
    // ADICIONADO: Fluxo para ler a preferência de notificações
    val areNotificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            // Por padrão, as notificações vêm ativadas
            preferences[NOTIFICATIONS_KEY] ?: true
        }

    // ADICIONADO: Função para salvar a preferência de notificações
    suspend fun saveNotificationsPreference(isEnabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[NOTIFICATIONS_KEY] = isEnabled
        }
    }
}