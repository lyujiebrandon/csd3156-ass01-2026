package edu.singaporetech.whackamole.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.singaporetech.whackamole.viewmodel.GameViewModel

/**
 * Game Over Screen - Displayed after the timer runs out.
 *
 * Shows:
 * - Final score with animation
 * - Game statistics (hits, misses, accuracy, max combo)
 * - Play Again button
 * - Back to Menu button
 */
@Composable
fun GameOverScreen(
    viewModel: GameViewModel,
    onPlayAgain: () -> Unit,
    onBackToMenu: () -> Unit
) {
    val score by viewModel.score.collectAsState()
    val hits by viewModel.hits.collectAsState()
    val misses by viewModel.misses.collectAsState()
    val playerName by viewModel.playerName.collectAsState()
    val difficulty by viewModel.difficulty.collectAsState()

    // Animate the score display appearing
    var showScore by remember { mutableStateOf(false) }
    val scoreScale by animateFloatAsState(
        targetValue = if (showScore) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "scoreScale"
    )

    LaunchedEffect(Unit) {
        showScore = true
    }

    val accuracy = if (hits + misses > 0) {
        (hits * 100) / (hits + misses)
    } else 0

    // Determine performance rating
    val rating = when {
        score >= 300 -> "🏆 Amazing!"
        score >= 200 -> "🌟 Great!"
        score >= 100 -> "👍 Good!"
        score >= 50 -> "🙂 Not Bad!"
        else -> "💪 Keep Trying!"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B5E20)) // Dark green
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Game Over title
        Text(
            text = "Game Over!",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = rating,
            fontSize = 28.sp,
            color = Color(0xFFFFEB3B)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Score card with animation
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scoreScale),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = playerName,
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$score",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFEB3B)
                )

                Text(
                    text = "points",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    GameOverStat(label = "Hits", value = "$hits", emoji = "✅")
                    GameOverStat(label = "Misses", value = "$misses", emoji = "❌")
                    GameOverStat(label = "Accuracy", value = "$accuracy%", emoji = "🎯")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Difficulty: $difficulty",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Play Again button
        Button(
            onClick = onPlayAgain,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF9800)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "🔄 Play Again",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Back to Menu button
        Button(
            onClick = {
                viewModel.resetGame()
                onBackToMenu()
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "🏠 Back to Menu",
                fontSize = 18.sp,
                color = Color.White
            )
        }
    }
}

/**
 * Single stat display for the game over screen.
 */
@Composable
private fun GameOverStat(label: String, value: String, emoji: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 24.sp)
        Text(
            text = value,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
    }
}
