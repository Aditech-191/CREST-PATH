package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.crestPathDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "crestpath_prefs"
)

class ThemePreferencesRepository(private val context: Context) {

    private val themeKey = stringPreferencesKey("theme_mode")

    val themeModeFlow: Flow<ThemeMode> = context.applicationContext.crestPathDataStore.data
        .map { prefs ->
            val raw = prefs[themeKey]
            ThemeMode.entries.firstOrNull { it.name == raw } ?: ThemeMode.SYSTEM
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.applicationContext.crestPathDataStore.edit { prefs ->
            prefs[themeKey] = mode.name
        }
    }
}
