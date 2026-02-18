package edu.singaporetech.whackamole.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.singaporetech.whackamole.data.entity.Score
import edu.singaporetech.whackamole.repository.GameRepository
import edu.singaporetech.whackamole.repository.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * GameViewModel manages all game state and logic.
 *
 * Game Flow:
 * 1. Player starts the game
 * 2. A mole appears at a random position in the 3x3 grid
 * 3. Player taps the mole to score points
 * 4. Mole disappears after a set time (based on difficulty)
 * 5. New mole appears at different position
 * 6. Game ends when timer reaches 0
 * 7. Score is saved to the database
 *
 * Uses StateFlow to reactively update the UI when state changes.
 */
class GameViewModel(
    private val gameRepository: GameRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // ==================== Game Configuration ====================

    companion object {
        const val GRID_SIZE = 9          // 3x3 grid = 9 holes
        const val GAME_DURATION = 30     // 30 seconds per game
        const val POINTS_PER_HIT = 10    // Points for hitting a mole
        const val COMBO_BONUS = 5        // Extra points per consecutive hit
    }

    // ==================== Game State ====================

    /** Which hole the mole is currently in (-1 = no mole visible) */
    private val _activeMoleIndex = MutableStateFlow(-1)
    val activeMoleIndex: StateFlow<Int> = _activeMoleIndex.asStateFlow()

    /** Current score */
    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    /** Time remaining in seconds */
    private val _timeRemaining = MutableStateFlow(GAME_DURATION)
    val timeRemaining: StateFlow<Int> = _timeRemaining.asStateFlow()

    /** Whether the game is currently running */
    private val _isGameActive = MutableStateFlow(false)
    val isGameActive: StateFlow<Boolean> = _isGameActive.asStateFlow()

    /** Whether the game has ended (to show game over screen) */
    private val _isGameOver = MutableStateFlow(false)
    val isGameOver: StateFlow<Boolean> = _isGameOver.asStateFlow()

    /** Current hit combo count */
    private val _combo = MutableStateFlow(0)
    val combo: StateFlow<Int> = _combo.asStateFlow()

    /** Total hits in this game */
    private val _hits = MutableStateFlow(0)
    val hits: StateFlow<Int> = _hits.asStateFlow()

    /** Total misses in this game */
    private val _misses = MutableStateFlow(0)
    val misses: StateFlow<Int> = _misses.asStateFlow()

    /** Whether the last tap was a hit (for UI feedback) */
    private val _lastTapWasHit = MutableStateFlow<Boolean?>(null)
    val lastTapWasHit: StateFlow<Boolean?> = _lastTapWasHit.asStateFlow()

    // ==================== Settings (from DataStore) ====================

    val soundEnabled = settingsRepository.soundEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = true
    )

    val vibrationEnabled = settingsRepository.vibrationEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = true
    )

    val difficulty = settingsRepository.difficulty.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = "Medium"
    )

    val playerName = settingsRepository.playerName.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = "Player"
    )

    // ==================== Leaderboard Data ====================

    val topScores = gameRepository.topScores.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    val allPlayers = gameRepository.allPlayers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    // ==================== Internal State ====================

    private var gameTimerJob: Job? = null
    private var moleJob: Job? = null

    /**
     * Get mole display duration based on difficulty.
     * Harder = mole disappears faster.
     */
    private fun getMoleVisibleDuration(): Long {
        return when (difficulty.value) {
            "Easy" -> 1500L    // 1.5 seconds
            "Medium" -> 1000L  // 1.0 seconds
            "Hard" -> 600L     // 0.6 seconds
            else -> 1000L
        }
    }

    /**
     * Get delay between moles based on difficulty.
     */
    private fun getMoleSpawnDelay(): Long {
        return when (difficulty.value) {
            "Easy" -> 500L     // 0.5 second gap
            "Medium" -> 300L   // 0.3 second gap
            "Hard" -> 150L     // 0.15 second gap
            else -> 300L
        }
    }

    // ==================== Game Actions ====================

    /**
     * Start a new game.
     * Resets all state and begins the timer and mole spawning.
     */
    fun startGame() {
        // Reset all game state
        _score.value = 0
        _hits.value = 0
        _misses.value = 0
        _combo.value = 0
        _timeRemaining.value = GAME_DURATION
        _isGameActive.value = true
        _isGameOver.value = false
        _activeMoleIndex.value = -1
        _lastTapWasHit.value = null

        // Start the countdown timer
        startTimer()

        // Start spawning moles
        startMoleSpawning()
    }

    /**
     * Handle player tapping on a hole.
     * @param index The index of the hole tapped (0-8)
     * @return true if it was a hit, false if miss
     */
    fun onHoleTapped(index: Int): Boolean {
        if (!_isGameActive.value) return false

        return if (index == _activeMoleIndex.value) {
            // HIT! Player tapped the mole
            _hits.value++
            _combo.value++

            // Calculate score: base points + combo bonus
            val comboPoints = (_combo.value - 1) * COMBO_BONUS
            _score.value += POINTS_PER_HIT + comboPoints

            // Hide the mole immediately after being hit
            _activeMoleIndex.value = -1

            _lastTapWasHit.value = true
            true
        } else {
            // MISS! Player tapped an empty hole
            _misses.value++
            _combo.value = 0  // Reset combo on miss
            _lastTapWasHit.value = false
            false
        }
    }

    /**
     * Start the countdown timer.
     * Runs on a coroutine, decrementing every second.
     */
    private fun startTimer() {
        gameTimerJob?.cancel()
        gameTimerJob = viewModelScope.launch {
            while (_timeRemaining.value > 0) {
                delay(1000L)  // Wait 1 second
                _timeRemaining.value--
            }
            // Time's up! End the game
            endGame()
        }
    }

    /**
     * Start the mole spawning loop.
     * Randomly places a mole in a different hole each time.
     */
    private fun startMoleSpawning() {
        moleJob?.cancel()
        moleJob = viewModelScope.launch {
            while (_isGameActive.value) {
                // Pick a random hole (different from current)
                var newIndex: Int
                do {
                    newIndex = (0 until GRID_SIZE).random()
                } while (newIndex == _activeMoleIndex.value)

                // Show the mole
                _activeMoleIndex.value = newIndex

                // Wait for mole to be visible
                delay(getMoleVisibleDuration())

                // If mole wasn't hit, hide it (it may already be -1 if hit)
                if (_activeMoleIndex.value == newIndex) {
                    _activeMoleIndex.value = -1
                }

                // Small delay before next mole appears
                delay(getMoleSpawnDelay())
            }
        }
    }

    /**
     * End the game, save the score, and update player stats.
     */
    private fun endGame() {
        _isGameActive.value = false
        _isGameOver.value = true
        _activeMoleIndex.value = -1

        // Cancel all running coroutines
        gameTimerJob?.cancel()
        moleJob?.cancel()

        // Save score to database
        viewModelScope.launch {
            val scoreRecord = Score(
                playerName = playerName.value,
                score = _score.value,
                difficulty = difficulty.value,
                hits = _hits.value,
                misses = _misses.value
            )
            gameRepository.insertScore(scoreRecord)

            // Update player stats
            gameRepository.updatePlayerStats(
                playerName = playerName.value,
                score = _score.value,
                hits = _hits.value,
                misses = _misses.value
            )
        }
    }

    /** Reset the game state back to initial (for returning to menu) */
    fun resetGame() {
        gameTimerJob?.cancel()
        moleJob?.cancel()
        _isGameActive.value = false
        _isGameOver.value = false
        _activeMoleIndex.value = -1
        _score.value = 0
        _timeRemaining.value = GAME_DURATION
    }

    // ==================== Settings Actions ====================

    fun setSoundEnabled(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setSoundEnabled(enabled)
    }

    fun setVibrationEnabled(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setVibrationEnabled(enabled)
    }

    fun setDifficulty(difficulty: String) = viewModelScope.launch {
        settingsRepository.setDifficulty(difficulty)
    }

    fun setPlayerName(name: String) = viewModelScope.launch {
        settingsRepository.setPlayerName(name)
    }

    // ==================== Leaderboard Actions ====================

    fun clearAllScores() = viewModelScope.launch {
        gameRepository.deleteAllScores()
    }

    fun clearAllData() = viewModelScope.launch {
        gameRepository.deleteAllScores()
        gameRepository.deleteAllPlayers()
    }

    override fun onCleared() {
        super.onCleared()
        gameTimerJob?.cancel()
        moleJob?.cancel()
    }
}

/**
 * Factory to create GameViewModel with required dependencies.
 * This is needed because our ViewModel requires constructor parameters.
 */
class GameViewModelFactory(
    private val gameRepository: GameRepository,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(gameRepository, settingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
