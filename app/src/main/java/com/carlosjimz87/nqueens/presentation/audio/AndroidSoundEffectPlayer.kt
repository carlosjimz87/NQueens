package com.carlosjimz87.nqueens.presentation.audio

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes
import com.carlosjimz87.nqueens.R

/**
 * An implementation of [SoundEffectPlayer] that uses Android's [MediaPlayer]
 * to play sound effects from raw resources. This encapsulates the sound playing logic
 * and provides methods to play specific sound effects for game events.
 */
class AndroidSoundEffectPlayer(
    private val context: Context
) : SoundEffectPlayer {

    override fun playQueenPlaced() {
        playRaw(R.raw.place)
    }

    override fun playQueenRemoved() {
        playRaw(R.raw.erase)
    }

    override fun playStart() {
        playRaw(R.raw.start)
    }

    override fun playConflict() {
        playRaw(R.raw.conflict)
    }

    override fun playSolved() {
        playRaw(R.raw.victory)
    }

    private fun playRaw(@RawRes resId: Int) {
        val mediaPlayer = MediaPlayer.create(context, resId) ?: return

        mediaPlayer.setOnCompletionListener { player ->
            player.release()
        }

        mediaPlayer.start()
    }
}