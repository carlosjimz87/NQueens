package com.carlosjimz87.rules.model

enum class BoardError {
    NoError,
    SizeTooSmall,   // n <= 3
    SizeTooBig      // n >= 21 to prevent performance issues
}