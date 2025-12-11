package com.carlosjimz87.nqueens.common

import com.carlosjimz87.nqueens.domain.error.BoardError


fun validateNQueensBoardSize(size: Int): BoardError {
    return when {
        size < 4 -> BoardError.SizeTooSmall
        size > 20 -> BoardError.SizeTooBig
        else -> BoardError.NoError
    }
}