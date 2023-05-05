package com.example.pointsrunner.presentation

import android.content.Context
import android.media.MediaPlayer
import android.media.SoundPool
import com.example.pointsrunner.R

class SoundManager(private val context: Context) {
    private val soundPool = SoundPool.Builder().setMaxStreams(5).build()
    var mediaPlayer: MediaPlayer? = null

    private val touchSoundId: Int = soundPool.load(context, R.raw.touch, 1)
    private lateinit var endMediaPlayer: MediaPlayer

    private fun play(soundId: Int, loop: Boolean = false) {
        soundPool.play(soundId, 1.0f, 1.0f, 0, if (loop) -1 else 0, 1.0f)
    }

    fun playMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.soundtrack).apply {
                isLooping = true
            }
        }
        mediaPlayer?.start()
    }

    fun playEndSound() {
        if (!::endMediaPlayer.isInitialized) {
            endMediaPlayer = MediaPlayer.create(context, R.raw.end).apply {
                setOnCompletionListener { mp ->
                    mp.release()
                }
            }
        }
        endMediaPlayer.start()
    }

    fun playTouchSound() {
        play(touchSoundId)
    }

    fun stopMediaPlayer() {
        mediaPlayer?.stop()
        mediaPlayer?.prepare()
    }

    fun release() {
        soundPool.release()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}