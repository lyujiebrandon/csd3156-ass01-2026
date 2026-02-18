package edu.singaporetech.whackamole.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

/**
 * SoundManager handles all sound effects for the game.
 * Uses SoundPool which is optimized for short sound clips.
 *
 * This satisfies the "Multimedia" advanced feature requirement.
 *
 * SETUP: You need to add sound files to res/raw/ folder:
 * - res/raw/hit.mp3       (when player hits a mole)
 * - res/raw/miss.mp3      (when player misses / taps empty hole)
 * - res/raw/game_over.mp3 (when the game ends)
 * - res/raw/countdown.mp3 (3-2-1 countdown beep)
 *
 * You can find free sound effects at:
 * - https://freesound.org
 * - https://mixkit.co/free-sound-effects/
 */
class SoundManager(context: Context) {

    private val soundPool: SoundPool
    private var hitSoundId: Int = 0
    private var missSoundId: Int = 0
    private var gameOverSoundId: Int = 0
    private var countdownSoundId: Int = 0
    private var isEnabled: Boolean = true

    init {
        // Configure audio attributes for game sounds
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        // Create SoundPool with max 4 simultaneous streams
        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(audioAttributes)
            .build()

        // Load sound files from res/raw/
        // NOTE: Make sure these files exist in your res/raw/ folder
        try {
            hitSoundId = soundPool.load(context, getResourceId(context, "hit"), 1)
            missSoundId = soundPool.load(context, getResourceId(context, "miss"), 1)
            gameOverSoundId = soundPool.load(context, getResourceId(context, "game_over"), 1)
            countdownSoundId = soundPool.load(context, getResourceId(context, "countdown"), 1)
        } catch (e: Exception) {
            // If sound files are missing, the game still works without sounds
            e.printStackTrace()
        }
    }

    /**
     * Helper to get resource ID by name.
     * Returns 0 if the resource doesn't exist.
     */
    private fun getResourceId(context: Context, name: String): Int {
        return context.resources.getIdentifier(name, "raw", context.packageName)
    }

    /** Enable or disable all sounds */
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    /** Play the hit sound (mole was tapped) */
    fun playHit() {
        if (isEnabled && hitSoundId != 0) {
            soundPool.play(hitSoundId, 1f, 1f, 1, 0, 1f)
        }
    }

    /** Play the miss sound (empty hole was tapped) */
    fun playMiss() {
        if (isEnabled && missSoundId != 0) {
            soundPool.play(missSoundId, 0.7f, 0.7f, 1, 0, 1f)
        }
    }

    /** Play the game over sound */
    fun playGameOver() {
        if (isEnabled && gameOverSoundId != 0) {
            soundPool.play(gameOverSoundId, 1f, 1f, 1, 0, 1f)
        }
    }

    /** Play the countdown beep */
    fun playCountdown() {
        if (isEnabled && countdownSoundId != 0) {
            soundPool.play(countdownSoundId, 0.8f, 0.8f, 1, 0, 1f)
        }
    }

    /** Release SoundPool resources when no longer needed */
    fun release() {
        soundPool.release()
    }
}
