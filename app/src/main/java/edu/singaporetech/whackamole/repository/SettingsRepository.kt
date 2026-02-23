package edu.singaporetech.whackamole.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.intPreferencesKey

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
        val STARTING_LEVEL = intPreferencesKey("starting_level")
        val PLAYER_NAME = stringPreferencesKey("player_name")
        val MUSIC_VOLUME = floatPreferencesKey("music_volume")
        val SFX_VOLUME = floatPreferencesKey("sfx_volume")
    }

    // ==================== Read Settings as Flows ====================

//    /** Whether sound effects are enabled (default: true) */
//    val soundEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
//        preferences[SOUND_ENABLED] ?: true
//    }

    val musicVolume: Flow<Float> = dataStore.data.map { it[MUSIC_VOLUME] ?: 0.5f }
    val sfxVolume: Flow<Float> = dataStore.data.map { it[SFX_VOLUME] ?: 0.5f }
    val startingLevel: Flow<Int> = dataStore.data.map { it[STARTING_LEVEL] ?: 1 }

    /** Whether vibration feedback is enabled (default: true) */
    val vibrationEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[VIBRATION_ENABLED] ?: true
    }

//    /** Current difficulty level (default: "Medium") */
//    val difficulty: Flow<String> = dataStore.data.map { preferences ->
//        preferences[DIFFICULTY] ?: "Medium"
//    }

    suspend fun setStartingLevel(level: Int) {
        dataStore.edit { it[STARTING_LEVEL] = level }
    }

    /** Saved player name (default: "Player") */
    val playerName: Flow<String> = dataStore.data.map { preferences ->
        preferences[PLAYER_NAME] ?: "Player"
    }

    // ==================== Write Settings ====================

    /** Toggle sound on/off */
//    suspend fun setSoundEnabled(enabled: Boolean) {
//        dataStore.edit { preferences ->
//            preferences[SOUND_ENABLED] = enabled
//        }
//    }

    suspend fun setMusicVolume(volume: Float) {
        dataStore.edit { it[MUSIC_VOLUME] = volume }
    }

    suspend fun setSfxVolume(volume: Float) {
        dataStore.edit { it[SFX_VOLUME] = volume }
    }

    /** Toggle vibration on/off */
    suspend fun setVibrationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[VIBRATION_ENABLED] = enabled
        }
    }

    /** Set difficulty level */
//    suspend fun setDifficulty(difficulty: String) {
//        dataStore.edit { preferences ->
//            preferences[STARTING_LEVEL] = difficulty
//        }
//    }

    /** Save player name */
    suspend fun setPlayerName(name: String) {
        dataStore.edit { preferences ->
            preferences[PLAYER_NAME] = name
        }
    }
}
