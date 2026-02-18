package edu.singaporetech.whackamole.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import edu.singaporetech.whackamole.data.entity.Player
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the players table.
 * Provides methods to manage player profiles and their cumulative stats.
 */
@Dao
interface PlayerDao {

    /** Get all players ordered by best score */
    @Query("SELECT * FROM players ORDER BY best_score DESC")
    fun getAllPlayers(): Flow<List<Player>>

    /** Get a specific player by name */
    @Query("SELECT * FROM players WHERE name = :name LIMIT 1")
    suspend fun getPlayerByName(name: String): Player?

    /** Insert a new player */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: Player)

    /** Update existing player stats */
    @Update
    suspend fun updatePlayer(player: Player)

    /** Delete all players (reset) */
    @Query("DELETE FROM players")
    suspend fun deleteAllPlayers()
}
