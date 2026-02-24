package edu.singaporetech.whackamole.repository

import androidx.annotation.WorkerThread
import edu.singaporetech.whackamole.data.dao.PlayerDao
import edu.singaporetech.whackamole.data.dao.ScoreDao
import edu.singaporetech.whackamole.data.entity.Player
import edu.singaporetech.whackamole.data.entity.Score
import kotlinx.coroutines.flow.Flow

/**
 * Author: Ryan Heng Hao Tian
 * Repository that manages all data operations for the game.
 * Acts as a single source of truth, abstracting the data sources
 * (Room database) from the rest of the app.
 *
 * The ViewModel interacts with this repository instead of
 * directly accessing the DAOs.
 */
class GameRepository(
    private val scoreDao: ScoreDao,
    private val playerDao: PlayerDao
) {

    // ==================== Score Operations ====================

    /** Get top 10 scores as a Flow (auto-updates UI) */
    val topScores: Flow<List<Score>> = scoreDao.getTopScores()

    /** Get all scores as a Flow */
    val allScores: Flow<List<Score>> = scoreDao.getAllScores()

    /** Save a new game score to the database */
    @WorkerThread
    suspend fun insertScore(score: Score) {
        scoreDao.insertScore(score)
    }

    /** Get scores filtered by difficulty */
    fun getScoresByDifficulty(difficulty: String): Flow<List<Score>> {
        return scoreDao.getScoresByDifficulty(difficulty)
    }

    /** Clear all scores */
    @WorkerThread
    suspend fun deleteAllScores() {
        scoreDao.deleteAllScores()
    }

    // ==================== Player Operations ====================

    /** Get all players as a Flow */
    val allPlayers: Flow<List<Player>> = playerDao.getAllPlayers()

    /** Get or create a player by name */
    @WorkerThread
    suspend fun getOrCreatePlayer(name: String): Player {
        val existingPlayer = playerDao.getPlayerByName(name)
        return if (existingPlayer != null) {
            existingPlayer
        } else {
            val newPlayer = Player(name = name)
            playerDao.insertPlayer(newPlayer)
            playerDao.getPlayerByName(name) ?: newPlayer
        }
    }

    /**
     * Update player stats after a game ends.
     * Increments games played, adds hits/misses, updates best score if beaten.
     */
    @WorkerThread
    suspend fun updatePlayerStats(
        playerName: String,
        score: Int,
        hits: Int,
        misses: Int
    ) {
        val player = getOrCreatePlayer(playerName)
        val updatedPlayer = player.copy(
            gamesPlayed = player.gamesPlayed + 1,
            totalHits = player.totalHits + hits,
            totalMisses = player.totalMisses + misses,
            bestScore = maxOf(player.bestScore, score)
        )
        playerDao.updatePlayer(updatedPlayer)
    }

    suspend fun getOverallBestScore(): Int {
        return scoreDao.getOverallBestScore() ?: 0
    }

    /** Clear all players */
    @WorkerThread
    suspend fun deleteAllPlayers() {
        playerDao.deleteAllPlayers()
    }
}
