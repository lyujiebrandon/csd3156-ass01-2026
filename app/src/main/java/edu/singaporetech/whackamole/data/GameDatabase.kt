package edu.singaporetech.whackamole.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import edu.singaporetech.whackamole.data.dao.PlayerDao
import edu.singaporetech.whackamole.data.dao.ScoreDao
import edu.singaporetech.whackamole.data.entity.Player
import edu.singaporetech.whackamole.data.entity.Score

/**
 * Author: Ryan Heng Hao Tian
 * Room database for the Whack-a-Mole game.
 *
 * Contains two tables:
 * 1. scores - stores individual game score records
 * 2. players - stores player profiles with cumulative stats
 *
 * Uses singleton pattern to ensure only one database instance exists.
 */
@Database(
    entities = [Score::class, Player::class],
    version = 1,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {

    // DAOs for each table
    abstract fun scoreDao(): ScoreDao
    abstract fun playerDao(): PlayerDao

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null

        /**
         * Get the singleton database instance.
         * Creates the database if it doesn't exist yet.
         */
        fun getDatabase(context: Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "whackamole_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
