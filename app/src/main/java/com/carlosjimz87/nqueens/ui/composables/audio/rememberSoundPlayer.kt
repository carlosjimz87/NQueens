package com.carlosjimz87.nqueens.ui.composables.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.carlosjimz87.nqueens.presentation.audio.AndroidSoundEffectPlayer

@Composable
fun rememberSoundPlayer(): AndroidSoundEffectPlayer {
    val context = LocalContext.current
    return remember(context.applicationContext) {
        AndroidSoundEffectPlayer(context.applicationContext)
    }
}