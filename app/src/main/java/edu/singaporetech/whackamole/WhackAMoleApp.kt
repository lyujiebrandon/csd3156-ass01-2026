package edu.singaporetech.whackamole

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import edu.singaporetech.whackamole.audio.SoundManager
import edu.singaporetech.whackamole.data.GameDatabase
import edu.singaporetech.whackamole.repository.GameRepository
import edu.singaporetech.whackamole.repository.SettingsRepository

/**
 * DataStore delegate - creates a single DataStore instance for the app.
 * This is defined at the top level as recommended by Android docs.
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "game_settings"
)

/**
 * Application class that initializes all dependencies.
 *
 * Uses lazy initialization so objects are only created when first needed.
 * This follows the same pattern as the Word app from lab sessions.
 *
 * IMPORTANT: Register this class in AndroidManifest.xml:
 * <application android:name=".WhackAMoleApp" ...>
 */
class WhackAMoleApp : Application() {

    // Database (lazy - created when first accessed)
    val database by lazy { GameDatabase.getDatabase(this) }

    // Repositories (lazy - created when first accessed)
    val gameRepository by lazy {
        GameRepository(database.scoreDao(), database.playerDao())
    }

    val settingsRepository by lazy {
        SettingsRepository(dataStore)
    }

    // Sound manager (lazy)
    val soundManager by lazy { SoundManager(this) }
}
