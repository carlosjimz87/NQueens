package com.carlosjimz87.nqueens.presentation.audio

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes
import com.carlosjimz87.nqueens.R

class AndroidSoundEffectPlayer(
    private val context: Context
) : SoundEffectPlayer {

    override fun playQueenPlaced() {
        playRaw(R.raw.place)
    }

    private fun playRaw(@RawRes resId: Int) {
        val mediaPlayer = MediaPlayer.create(context, resId) ?: return

        mediaPlayer.setOnCompletionListener { player ->
            player.release()
        }

        mediaPlayer.start()
    }
}