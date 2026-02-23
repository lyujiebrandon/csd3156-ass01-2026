package edu.singaporetech.whackamole.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.media.MediaPlayer

/**
 * SoundManager handles all sound effects for the game.
 * Uses SoundPool which is optimized for short sound clips.
 *
 * This satisfies the "Multimedia" advanced feature requirement.
 *
 *  Audio and Sound Effects files can be found in the res/raw/ folder:
 * - res/raw/countdown.mp3 (3-2-1 countdown beep)
 * - res/raw/game_music.mp3 (in-game music)
 * - res/raw/game_over.mp3 (when the game ends)
 * - res/raw/hit.mp3       (when player hits a mole)
 * - res/raw/menu_music.mp3 (menu music)
 * - res/raw/miss.mp3      (when player misses / taps empty hole)
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
    private var menuMusic: MediaPlayer? = null
    private var gameMusic: MediaPlayer? = null
    private var currentTrack: String? = null
    private var sfxVolume: Float = 0.5f
    private var musicVolume: Float = 0.5f

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

    fun setMusicVolume(volume: Float) {
        musicVolume = volume
        menuMusic?.setVolume(volume, volume)
        gameMusic?.setVolume(volume, volume)
    }

    fun setSfxVolume(volume: Float) {
        sfxVolume = volume
    }

    fun switchToMenuMusic(context: Context) {
        if (currentTrack == "menu" && menuMusic?.isPlaying == true) return // tighten the guard
        currentTrack = "menu"
        gameMusic?.stop()
        gameMusic?.release()
        gameMusic = null
        val resourceId = getResourceId(context, "menu_music")
        if (resourceId == 0) return
        menuMusic = MediaPlayer.create(context, resourceId)?.apply {
            isLooping = true
            setVolume(musicVolume, musicVolume)
            start()
        }
    }

    fun switchToGameMusic(context: Context) {
        if (currentTrack == "game" && gameMusic?.isPlaying == true) return // tighten the guard
        currentTrack = "game"
        menuMusic?.stop()
        menuMusic?.release()
        menuMusic = null
        val resourceId = getResourceId(context, "game_music")
        if (resourceId == 0) return
        gameMusic = MediaPlayer.create(context, resourceId)?.apply {
            isLooping = true
            setVolume(musicVolume, musicVolume)
            start()
        }
    }

    /** Play the hit sound (mole was tapped) */
    fun playHit() {
        if (hitSoundId != 0)
            soundPool.play(hitSoundId, sfxVolume, sfxVolume, 1, 0, 1f)
    }

    /** Play the miss sound (empty hole was tapped) */
    fun playMiss() {
        if (missSoundId != 0)
            soundPool.play(missSoundId, sfxVolume, sfxVolume, 1, 0, 1f)
    }

    /** Play the game over sound */
    fun playGameOver() {
        if (gameOverSoundId != 0)
            soundPool.play(gameOverSoundId, sfxVolume, sfxVolume, 1, 0, 1f)
    }

    /** Play the countdown beep */
    fun playCountdown() {
        if (countdownSoundId != 0)
            soundPool.play(countdownSoundId, sfxVolume, sfxVolume, 1, 0, 1f)
    }

    fun stopAllMusic() {
        currentTrack = null
        menuMusic?.stop()
        menuMusic?.release()
        menuMusic = null
        gameMusic?.stop()
        gameMusic?.release()
        gameMusic = null
    }
}
