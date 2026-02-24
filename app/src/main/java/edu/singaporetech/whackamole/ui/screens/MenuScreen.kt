package edu.singaporetech.whackamole.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.singaporetech.whackamole.WhackAMoleApp
import edu.singaporetech.whackamole.viewmodel.GameViewModel

/**
 * Author: Hong Xian Xiang
 *
 * Main Menu Screen - the first screen the player sees.
 *
 * Features:
 * - Animated title
 * - Player name input (saved to DataStore)
 * - Start Game button
 * - Leaderboard button
 * - Settings button
 */
@Composable
fun MenuScreen(
    viewModel: GameViewModel,
    onStartGame: () -> Unit,
    onLeaderboard: () -> Unit,
    onSettings: () -> Unit
) {

    val savedName by viewModel.playerName.collectAsState()
    val topScores by viewModel.topScores.collectAsState()
    val bestScore = topScores.maxOfOrNull { it.score } ?: 0     // Best score from leaderboard
    val unlockedLevels = viewModel.getUnlockedStartingLevels(bestScore)
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")     // Animated bouncing mole emoji for the title
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "moleScale"
    )

    var localName by remember(savedName) { mutableStateOf(savedName) }
    var showDifficultyDialog by remember { mutableStateOf(false) }
    var selectedLevel by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF4CAF50)) // Green background
            .padding(horizontal = 40.dp, vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated mole icon
        Text(
            text = "🐹",
            fontSize = 80.sp,
            modifier = Modifier.scale(scale)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Game title
        Text(
            text = "Whack-a-Mole!",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tap the moles before they hide!",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Player name input
        OutlinedTextField(
            value = localName,
            onValueChange = { localName = it },
            label = { Text("Player Name", color = Color.White) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(0.8f),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Watch for a selection and only then navigate
        LaunchedEffect(selectedLevel) {
            selectedLevel?.let { level ->
                viewModel.setStartingLevel(level)
                selectedLevel = null
                showDifficultyDialog = false
                onStartGame()
            }
        }

        // Start Game button
        Button(
            onClick = { showDifficultyDialog = true
                viewModel.setPlayerName(localName)
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF9800)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "🎮   Start Game",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Difficulty selection dialog
        if (showDifficultyDialog) {
            AlertDialog(
                onDismissRequest = { showDifficultyDialog = false },
                containerColor = Color(0xFF37474F),
                title = {
                    Text(
                        text = "Choose Starting Difficulty",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DifficultyDialogOption(
                            label = "🐌 Easy",
                            description = "Start at Level 1",
                            unlockInfo = "Always unlocked",
                            isUnlocked = true,
                            onClick = {
                                viewModel.setStartingLevel(1)
                                viewModel.startGameAtLevel(1)
                                showDifficultyDialog = false
                                onStartGame()
                            }
                        )
                        DifficultyDialogOption(
                            label = "🐇 Medium",
                            description = "Start at Level 3",
                            unlockInfo = if (3 in unlockedLevels) "Unlocked" else "Reach 1000 points to unlock",
                            isUnlocked = 3 in unlockedLevels,
                            onClick = {
                                if (3 in unlockedLevels) {
                                    viewModel.setStartingLevel(3)
                                    viewModel.startGameAtLevel(3)
                                    showDifficultyDialog = false
                                    onStartGame()
                                }
                            }
                        )
                        DifficultyDialogOption(
                            label = "⚡ Hard",
                            description = "Start at Level 5",
                            unlockInfo = if (5 in unlockedLevels) "Unlocked" else "Reach 1700 points to unlock",
                            isUnlocked = 5 in unlockedLevels,
                            onClick = {
                                if (5 in unlockedLevels) {
                                    viewModel.setStartingLevel(5)
                                    viewModel.startGameAtLevel(5)
                                    showDifficultyDialog = false
                                    onStartGame()
                                }
                            }
                        )
                    }
                },
                confirmButton = {}
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ==================== Leaderboard ====================
        Button(
            onClick = onLeaderboard,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "🏆   Leaderboard",
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ======================== Settings button ========================
        Button(
            onClick = onSettings,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF607D8B)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "⚙️   Settings",
                fontSize = 20.sp
            )
        }
    }
}

/**
 * Difficulty selection option in the difficulty selection dialog.
 */
@Composable
private fun DifficultyDialogOption(
    label: String,
    description: String,
    unlockInfo: String,
    isUnlocked: Boolean,
    onClick: () -> Unit
) {
    val alpha = if (isUnlocked) 1f else 0.4f

    // Card for each difficulty option
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = label, color = Color.White.copy(alpha = alpha),
                    fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = description, color = Color.White.copy(alpha = alpha * 0.7f),
                    fontSize = 14.sp)
                Text(text = unlockInfo,
                    color = if (isUnlocked) Color(0xFF4CAF50) else Color(0xFFF44336),
                    fontSize = 12.sp)
            }
            if (!isUnlocked) Text(text = "🔒", fontSize = 24.sp)
        }
    }
}

