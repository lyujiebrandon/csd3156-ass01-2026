package edu.singaporetech.whackamole

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import edu.singaporetech.whackamole.audio.SoundManager
import edu.singaporetech.whackamole.ui.screens.GameOverScreen
import edu.singaporetech.whackamole.ui.screens.GameScreen
import edu.singaporetech.whackamole.ui.screens.LeaderboardScreen
import edu.singaporetech.whackamole.ui.screens.MenuScreen
import edu.singaporetech.whackamole.ui.screens.SettingsScreen
import edu.singaporetech.whackamole.viewmodel.GameViewModel
import edu.singaporetech.whackamole.viewmodel.GameViewModelFactory

/**
 * MainActivity - Entry point of the Whack-a-Mole game.
 *
 * Uses Jetpack Navigation Compose for screen navigation.
 * The GameViewModel is shared across all screens using the activity scope.
 */
class MainActivity : ComponentActivity() {

    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get application-level dependencies
        val app = application as WhackAMoleApp
        soundManager = app.soundManager
        val viewModelFactory = GameViewModelFactory(
            app.gameRepository,
            app.settingsRepository
        )

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF4CAF50)
            ) {
                WhackAMoleNavigation(viewModelFactory = viewModelFactory)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        soundManager.stopAllMusic()
    }

    override fun onResume() {
        super.onResume()
    }
}

/**
 * Navigation graph for the entire app.
 */
@Composable
fun WhackAMoleNavigation(viewModelFactory: GameViewModelFactory) {
    val navController = rememberNavController()

    // Shared ViewModel across all screens
    val gameViewModel: GameViewModel = viewModel(factory = viewModelFactory)
    val context = LocalContext.current
    val soundManager = (context.applicationContext as WhackAMoleApp).soundManager
    val musicVolume by gameViewModel.musicVolume.collectAsState()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    LaunchedEffect(currentRoute, musicVolume) {
        when (currentRoute) {
            "game" -> soundManager.switchToGameMusic(context)
            "menu" -> soundManager.switchToMenuMusic(context)
            else -> soundManager.switchToMenuMusic(context)
        }
        soundManager.setMusicVolume(musicVolume)
    }

    // Stop music when navigation leaves the app entirely
    DisposableEffect(Unit) {
        onDispose { soundManager.stopAllMusic() }
    }
    NavHost(
        navController = navController,
        startDestination = "menu"
    ) {
        // Menu Screen
        composable("menu") {
            MenuScreen(
                viewModel = gameViewModel,
                onStartGame = {
                    navController.navigate("game") {
                        popUpTo("menu") { inclusive = false }
                    }
                },
                onLeaderboard = {
                    navController.navigate("leaderboard")
                },
                onSettings = {
                    navController.navigate("settings")
                }
            )
        }

        // Game Screen
        composable("game") {
            GameScreen(
                viewModel = gameViewModel,
                onGameOver = {
                    navController.navigate("game_over") {
                        popUpTo("menu") { inclusive = false }
                    }
                }
            )
        }

        // Game Over Screen
        composable("game_over") {
            GameOverScreen(
                viewModel = gameViewModel,
                onPlayAgain = {
                    navController.navigate("game") {
                        popUpTo("menu") { inclusive = false }
                    }
                },
                onBackToMenu = {
                    navController.navigate("menu") {
                        popUpTo("menu") { inclusive = true }
                    }
                }
            )
        }

        // Leaderboard Screen
        composable("leaderboard") {
            LeaderboardScreen(
                viewModel = gameViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Settings Screen
        composable("settings") {
            SettingsScreen(
                viewModel = gameViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}