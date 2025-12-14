package com.carlosjimz87.rules.board

import com.carlosjimz87.rules.model.BoardError

class BoardCheckerImpl : BoardChecker {
    override fun validateSize(size: Int): BoardError {
        return when {
            size < 4 -> BoardError.SizeTooSmall
            size > 20 -> BoardError.SizeTooBig
            else -> BoardError.NoError
        }
    }
}