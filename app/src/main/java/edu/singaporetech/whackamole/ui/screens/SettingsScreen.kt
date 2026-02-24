package edu.singaporetech.whackamole.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.singaporetech.whackamole.viewmodel.GameViewModel
import edu.singaporetech.whackamole.WhackAMoleApp

/**
 * Author: Ong Angie, Reagan Tang Rui Feng
 *
 * Settings Screen - Allows players to configure game preferences.
 *
 * All settings are saved to Preferences DataStore, so they persist
 * even after the app is closed or the device is restarted.
 *
 * Settings:
 * - Sound effects on/off
 * - Vibration feedback on/off
 * - Difficulty level (Easy / Medium / Hard)
 * - Clear all data option
 */
@Composable
fun SettingsScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val musicVolume by viewModel.musicVolume.collectAsState()
    val sfxVolume by viewModel.sfxVolume.collectAsState()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()
    val soundManager = (LocalContext.current.applicationContext as WhackAMoleApp).soundManager

    /**
     * Settings screen layout.
     */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF37474F)) // Dark grey background
            .padding(horizontal = 40.dp, vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ======================== Title ========================
        Text(
            text = "⚙️ Settings",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ==================== Volume Sliders ====================
        SettingsCard {
            Column {
                Text("🎵 Music Volume", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Slider(
                    value = musicVolume,
                    onValueChange = {
                        viewModel.setMusicVolume(it)
                        soundManager.setMusicVolume(it)
                    },
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF4CAF50), activeTrackColor = Color(
                        0xFF96F398
                    )
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("🔊 SFX Volume", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Slider(
                    value = sfxVolume,
                    onValueChange = {
                        viewModel.setSfxVolume(it)
                        soundManager.setSfxVolume(it)
                    },
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF4CAF50), activeTrackColor = Color(
                        0xFF96F398
                    )
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ==================== Vibration Toggle ====================
        SettingsCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📳 Vibration",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Vibrate when hitting a mole",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                }
                Switch(
                    checked = vibrationEnabled,
                    onCheckedChange = { viewModel.setVibrationEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF4CAF50),
                        checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ==================== Clear Data ====================
        SettingsCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🗑️ Clear All Data",
                        color = Color(0xFFF44336),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Delete all scores and player stats",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                }
                Button(
                    onClick = { viewModel.clearAllData() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Clear")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // ======================== Back button ========================
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
 * Reusable card container for settings items.
 */
@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

