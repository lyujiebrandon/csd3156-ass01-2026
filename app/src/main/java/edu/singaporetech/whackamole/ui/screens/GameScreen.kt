package edu.singaporetech.whackamole.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
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
 * Game Screen - The main gameplay area.
 *
 * Layout:
 * - Top: Score and Timer display
 * - Middle: 3x3 grid of mole holes
 * - Bottom: Combo indicator
 *
 * Features:
 * - Animated mole pop-up/down (Animation feature)
 * - Sound effects on hit/miss (Multimedia feature)
 * - Vibration feedback on hit (Sensor feature)
 */
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onGameOver: () -> Unit
) {
    val activeMoleIndex by viewModel.activeMoleIndex.collectAsState()
    val score by viewModel.score.collectAsState()
    val timeRemaining by viewModel.timeRemaining.collectAsState()
    val isGameOver by viewModel.isGameOver.collectAsState()
    val combo by viewModel.combo.collectAsState()
    val hits by viewModel.hits.collectAsState()
    val misses by viewModel.misses.collectAsState()
//    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val sfxVolume by viewModel.sfxVolume.collectAsState()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()
//    val difficulty by viewModel.difficulty.collectAsState()
    val context = LocalContext.current
    val soundManager = (context.applicationContext as WhackAMoleApp).soundManager
    val currentLevel by viewModel.currentLevel.collectAsState()
    val hitsThisLevel by viewModel.hitsThisLevel.collectAsState()
    var gameStarted by remember { mutableStateOf(false) }
    val isCountingDown by viewModel.isCountingDown.collectAsState()
    val countdownValue by viewModel.countdownValue.collectAsState()
    val playCountdownSound by viewModel.playCountdownSound.collectAsState()
    val gameId by viewModel.gameId.collectAsState()

    // Start the game when this screen appears
    LaunchedEffect(Unit) {
        soundManager.switchToGameMusic(context)
        gameStarted = true
    }

    // Navigate to game over screen when game ends
    LaunchedEffect(gameId, isGameOver) {
        if (isGameOver && gameStarted) {
            if (sfxVolume > 0f) soundManager.playGameOver()
            onGameOver()
        }
    }

    LaunchedEffect(gameId) {
        if (sfxVolume > 0f) {
            soundManager.playCountdown()
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF83986A))
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
//                .background(Color(0xFF9AB678)) // Light green background
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ==================== Top Bar: Score & Timer ====================
            ScoreAndTimerBar(score = score, timeRemaining = timeRemaining, combo = combo)

            Spacer(modifier = Modifier.height(8.dp))

            // Timer progress bar
            LinearProgressIndicator(
                progress = { timeRemaining.toFloat() / GameViewModel.GAME_DURATION.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    timeRemaining <= 5 -> Color.Red
                    timeRemaining <= 10 -> Color(0xFFFF9800)
                    else -> Color(0xFF39913D)
                },
                trackColor = Color.White.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Level $currentLevel",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Next level: $hitsThisLevel/${GameViewModel.HITS_PER_LEVEL} hits",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // ==================== 3x3 Mole Grid ====================
            MoleGrid(
                activeMoleIndex = activeMoleIndex,
                soundEnabled = sfxVolume > 0f,
                vibrationEnabled = vibrationEnabled,
                context = context,
                soundManager = soundManager,
                onHoleTapped = { index ->
                    viewModel.onHoleTapped(index)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ==================== Stats Bar ====================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "Hits", value = hits.toString(), color = Color(0xFF39913D))
                StatItem(label = "Misses", value = misses.toString(), color = Color(0xFFB93329))
                StatItem(
                    label = "Accuracy",
                    value = if (hits + misses > 0) {
                        "${(hits * 100 / (hits + misses))}%"
                    } else "0%",
                    color = Color(0xFF0A62A8)
                )
            }
        }
        if (isCountingDown) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = when (countdownValue) {
                            3 -> "3"
                            2 -> "2"
                            1 -> "1"
                            else -> "GO!"
                        },
                        fontSize = 120.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFEB3B)
                    )
                }
            }
        }
    }
}

/**
 * Score and Timer display bar at the top of the game screen.
 */
@Composable
private fun ScoreAndTimerBar(score: Int, timeRemaining: Int, combo: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.Black.copy(alpha = 0.2f),
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Score
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Score",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
            Text(
                text = "$score",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Combo
        if (combo > 1) {
            Text(
                text = "🔥 x$combo",
                color = Color(0xFFFFEB3B),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Timer
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Time",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
            Text(
                text = "${timeRemaining}s",
                color = if (timeRemaining <= 5) Color.Red else Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * The 3x3 grid of mole holes.
 * Each hole can contain a mole (when activeMoleIndex matches).
 *
 * Uses animations for mole pop-up effect (Animation feature).
 */
@Composable
private fun MoleGrid(
    activeMoleIndex: Int,
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    context: Context,
    soundManager: edu.singaporetech.whackamole.audio.SoundManager,
    onHoleTapped: (Int) -> Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f), // Square grid
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF795548) // Brown "dirt" background
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Create 3 rows of 3 holes each
            for (row in 0 until 3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (col in 0 until 3) {
                        val index = row * 3 + col
                        val hasMole = (index == activeMoleIndex)

                        MoleHole(
                            hasMole = hasMole,
                            onTap = {
                                val isHit = onHoleTapped(index)
                                // Play sound based on hit/miss
                                if (soundEnabled) {
                                    if (isHit) soundManager.playHit()
                                    else soundManager.playMiss()
                                }
                                // Vibrate on hit
                                if (vibrationEnabled && isHit) {
                                    vibrate(context)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * A single mole hole in the grid.
 * Shows an empty hole or a mole with pop-up animation.
 *
 * Animation: The mole scales from 0 to 1 when appearing (spring animation).
 */
@Composable
private fun MoleHole(
    hasMole: Boolean,
    onTap: () -> Unit
) {
    // Animate mole scale: 0 (hidden) -> 1 (fully visible)
    val moleScale by animateFloatAsState(
        targetValue = if (hasMole) 1f else 0f,
        animationSpec = if (hasMole) {
            spring(dampingRatio = 0.4f, stiffness = 300f) // Bouncy pop-up
        } else {
            tween(durationMillis = 150) // Quick hide
        },
        label = "moleScale"
    )

    Box(
        modifier = Modifier
            .size(90.dp)
            .clip(CircleShape)
            .background(Color(0xFF5D4037)) // Dark brown hole
            .border(3.dp, Color(0xFF3E2723), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // No ripple - we handle feedback ourselves
            ) { onTap() },
        contentAlignment = Alignment.Center
    ) {
        if (moleScale > 0.01f) {
            // Mole is visible - show it with scale animation
            Text(
                text = "🐹",
                fontSize = 48.sp,
                modifier = Modifier.scale(moleScale),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Small stat display item (Hits, Misses, Accuracy).
 */
@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(
                Color.Black.copy(alpha = 0.2f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        Text(text = value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

/**
 * Trigger a short vibration for haptic feedback.
 * Uses the appropriate API based on Android version.
 */
private fun vibrate(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(
                VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        }
    } catch (e: Exception) {
        // Vibration not available on this device
        e.printStackTrace()
    }
}
