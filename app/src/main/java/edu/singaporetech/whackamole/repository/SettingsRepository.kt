package edu.singaporetech.whackamole.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for managing app settings using Preferences DataStore.
 *
 * DataStore is used instead of SharedPreferences because:
 * - It's asynchronous (won't block the UI thread)
 * - It uses Flow for reactive updates
 * - It's the recommended modern approach
 *
 * Settings persisted:
 * - Sound on/off
 * - Vibration on/off
 * - Difficulty level (Easy/Medium/Hard)
 * - Player name
 */
class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    // Keys for each setting stored in DataStore
    companion object {
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val DIFFICULTY = stringPreferencesKey("difficulty")
        val PLAYER_NAME = stringPreferencesKey("player_name")
    }

    // ==================== Read Settings as Flows ====================

    /** Whether sound effects are enabled (default: true) */
    val soundEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SOUND_ENABLED] ?: true
    }

    /** Whether vibration feedback is enabled (default: true) */
    val vibrationEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[VIBRATION_ENABLED] ?: true
    }

    /** Current difficulty level (default: "Medium") */
    val difficulty: Flow<String> = dataStore.data.map { preferences ->
        preferences[DIFFICULTY] ?: "Medium"
    }

    /** Saved player name (default: "Player") */
    val playerName: Flow<String> = dataStore.data.map { preferences ->
        preferences[PLAYER_NAME] ?: "Player"
    }

    // ==================== Write Settings ====================

    /** Toggle sound on/off */
    suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SOUND_ENABLED] = enabled
        }
    }

    /** Toggle vibration on/off */
    suspend fun setVibrationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[VIBRATION_ENABLED] = enabled
        }
    }

    /** Set difficulty level */
    suspend fun setDifficulty(difficulty: String) {
        dataStore.edit { preferences ->
            preferences[DIFFICULTY] = difficulty
        }
    }

    /** Save player name */
    suspend fun setPlayerName(name: String) {
        dataStore.edit { preferences ->
            preferences[PLAYER_NAME] = name
        }
    }
}
