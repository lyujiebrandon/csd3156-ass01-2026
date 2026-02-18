package edu.singaporetech.whackamole.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Score entity representing a single game score record.
 * This is one of the multiple tables in our Room database.
 *
 * Each time a game ends, a new Score record is saved.
 */
@Entity(tableName = "scores")
data class Score(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "player_name")
    val playerName: String,

    @ColumnInfo(name = "score")
    val score: Int,

    @ColumnInfo(name = "difficulty")
    val difficulty: String,  // "Easy", "Medium", "Hard"

    @ColumnInfo(name = "hits")
    val hits: Int,

    @ColumnInfo(name = "misses")
    val misses: Int,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
