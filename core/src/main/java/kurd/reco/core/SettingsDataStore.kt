package kurd.reco.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {
    companion object {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val MATERIAL_THEME = booleanPreferencesKey("material_theme")
        val EXTERNAL_PLAYER = stringPreferencesKey("external_player")
        val SUBTITLE_SIZE = floatPreferencesKey("subtitle_size")
        val SHOW_TITLE = booleanPreferencesKey("show_title")
        val USE_VPN = booleanPreferencesKey("use_vpn")
        val FORCE_HIGHEST_QUALITY = booleanPreferencesKey("force_highest_quality")
    }

    private fun <T> savePreference(key: Preferences.Key<T>, value: T) {
        runBlocking {
            context.dataStore.edit { preferences ->
                preferences[key] = value
            }
        }
    }

    val darkThemeEnabled: Flow<Boolean> = context.dataStore.data
        .map {
            it[DARK_MODE] ?: true
        }

    fun setDarkMode(enabled: Boolean) = savePreference(DARK_MODE, enabled)

    val materialThemeEnabled: Flow<Boolean> = context.dataStore.data
        .map {
            it[MATERIAL_THEME] ?: false
        }

    fun saveMaterialTheme(enabled: Boolean) = savePreference(MATERIAL_THEME, enabled)

    val externalPlayer: Flow<String> = context.dataStore.data
        .map {
            it[EXTERNAL_PLAYER] ?: ""
        }

    fun setExternalPlayer(player: String) = savePreference(EXTERNAL_PLAYER, player)

    val subtitleSize: Flow<Float> = context.dataStore.data
        .map {
            it[SUBTITLE_SIZE] ?: 21f
        }

    fun setSubtitleSize(size: Float) = savePreference(SUBTITLE_SIZE, size)

    val showTitleEnabled: Flow<Boolean> = context.dataStore.data
        .map {
            it[SHOW_TITLE] ?: true
        }

    fun setShowTitle(enabled: Boolean) = savePreference(SHOW_TITLE, enabled)

    val useVpnEnabled: Flow<Boolean> = context.dataStore.data
        .map {
            it[USE_VPN] ?: false
        }

    fun setUseVpn(enabled: Boolean) = savePreference(USE_VPN, enabled)

    val forceHighestQualityEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[FORCE_HIGHEST_QUALITY] ?: true }

    fun setForceHighestQuality(enabled: Boolean) = savePreference(FORCE_HIGHEST_QUALITY, enabled)
}