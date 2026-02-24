package edu.singaporetech.whackamole.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Author: Ryan Heng Hao Tian
 *
 * Player entity representing a player profile with cumulative stats.
 * This is the second table in our Room database (multiple tables requirement).
 *
 * Tracks lifetime stats like total games played, total hits, and best score.
 */
@Entity(tableName = "players")
data class Player(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "games_played")
    val gamesPlayed: Int = 0,

    @ColumnInfo(name = "total_hits")
    val totalHits: Int = 0,

    @ColumnInfo(name = "total_misses")
    val totalMisses: Int = 0,

    @ColumnInfo(name = "best_score")
    val bestScore: Int = 0
)
