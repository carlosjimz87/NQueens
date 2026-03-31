package com.carlosjimz87.rules.model

enum class BoardError {
    NoError,
    SizeTooSmall,   // n <= [Constants.MIN_BOARD_SIZE]
    SizeTooBig      // n >= [Constants.MAX_BOARD_SIZE] to prevent performance issues
}