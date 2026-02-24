package edu.singaporetech.whackamole.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.singaporetech.whackamole.data.entity.Score
import edu.singaporetech.whackamole.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Author: Ong Angie
 * Leaderboard Screen - Displays top 10 scores from the Room database.
 *
 * Shows:
 * - Ranked list of scores with player names
 * - Score details (difficulty, accuracy)
 * - Clear scores button
 * - Back button
 *
 * The scores are observed via StateFlow from the ViewModel,
 * so the list auto-updates when new scores are added.
 */
@Composable
fun LeaderboardScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val topScores by viewModel.topScores.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A237E)) // Dark blue background
            .padding( horizontal = 40.dp, vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "🏆 Leaderboard",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        if (topScores.isEmpty()) {
            // No scores yet
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "No scores yet!\nPlay a game to see your scores here.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.weight(1f))
        } else {
            // Scores list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    items = topScores,
                    key = { _, score -> score.id }
                ) { index, score ->
                    ScoreItem(rank = index + 1, score = score)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Clear scores button
        if (topScores.isNotEmpty()) {
            Button(
                onClick = { viewModel.clearAllScores() },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF44336)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = "🗑️ Clear All Scores", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Back button
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "← Back to Menu",
                fontSize = 18.sp,
                color = Color.White
            )
        }
    }
}

/**
 * Single score item in the leaderboard list.
 */
@Composable
private fun ScoreItem(rank: Int, score: Score) {
    // Medal emoji for top 3
    val medal = when (rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> "#$rank"
    }

    // Highlight color for top 3
    val cardColor = when (rank) {
        1 -> Color(0xFFFFD700).copy(alpha = 0.2f)
        2 -> Color(0xFFC0C0C0).copy(alpha = 0.15f)
        3 -> Color(0xFFCD7F32).copy(alpha = 0.15f)
        else -> Color.White.copy(alpha = 0.08f)
    }

    val accuracy = if (score.hits + score.misses > 0) {
        (score.hits * 100) / (score.hits + score.misses)
    } else 0

    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(score.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank
            Text(
                text = medal,
                fontSize = if (rank <= 3) 28.sp else 16.sp,
                modifier = Modifier.padding(end = 12.dp)
            )

            // Player info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = score.playerName,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${score.difficulty} • $accuracy% accuracy • $dateString",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }

            // Score
            Text(
                text = "${score.score}",
                color = Color(0xFFFFEB3B),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
