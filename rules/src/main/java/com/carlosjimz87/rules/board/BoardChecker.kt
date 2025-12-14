package com.carlosjimz87.rules.board

import com.carlosjimz87.rules.model.BoardError

interface BoardChecker {
    fun validateSize(size: Int): BoardError
}