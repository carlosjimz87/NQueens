package com.carlosjimz87.nqueens.domain.error

enum class BoardError {
    NoError,
    SizeTooSmall,   // n <= 3
    SizeTooBig      // n >= 21 to prevent performance issues
}