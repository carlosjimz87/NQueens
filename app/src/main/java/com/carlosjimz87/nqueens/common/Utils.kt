package com.carlosjimz87.nqueens.common

import com.carlosjimz87.nqueens.domain.error.BoardError


fun validateNQueensBoardSize(size: Int): BoardError {
    return when {
        size < 4 -> BoardError.SizeTooSmall
        size > 20 -> BoardError.SizeTooBig
        else -> BoardError.NoError
    }
}

fun formatElapsed(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}