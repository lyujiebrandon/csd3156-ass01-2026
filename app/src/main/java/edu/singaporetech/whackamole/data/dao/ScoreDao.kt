package edu.singaporetech.whackamole.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import edu.singaporetech.whackamole.data.entity.Score
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the scores table.
 * Provides methods to insert, query, and delete score records.
 *
 * Flow return types automatically notify observers when data changes.
 */
@Dao
interface ScoreDao {

    /** Get all scores ordered by highest score first */
    @Query("SELECT * FROM scores ORDER BY score DESC")
    fun getAllScores(): Flow<List<Score>>

    /** Get top 10 scores for the leaderboard */
    @Query("SELECT * FROM scores ORDER BY score DESC LIMIT 10")
    fun getTopScores(): Flow<List<Score>>

    /** Get scores filtered by difficulty */
    @Query("SELECT * FROM scores WHERE difficulty = :difficulty ORDER BY score DESC")
    fun getScoresByDifficulty(difficulty: String): Flow<List<Score>>

    /** Insert a new score record */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: Score)

    /** Delete all scores (reset) */
    @Query("DELETE FROM scores")
    suspend fun deleteAllScores()

    @Query("SELECT MAX(score) FROM scores WHERE player_name = :playerName")
    suspend fun getBestScoreForPlayer(playerName: String): Int?

    @Query("SELECT MAX(score) FROM scores")
    suspend fun getOverallBestScore(): Int?
}
