package com.carlosjimz87.nqueens.common

import com.carlosjimz87.rules.model.BoardError
import com.carlosjimz87.rules.board.BoardCheckerImpl
import org.junit.Assert.assertEquals
import org.junit.Test

class ValidateNQueensBoardSizeTest {
    val boardChecker = BoardCheckerImpl()
    @Test
    fun `size 4 to 20 returns NoError`() {
        for (n in 4..20) {
            assertEquals(BoardError.NoError, boardChecker.validateSize(n))
        }
    }

    @Test
    fun `size less or equal 3 returns SizeTooSmall`() {
        assertEquals(BoardError.SizeTooSmall, boardChecker.validateSize(0))
        assertEquals(BoardError.SizeTooSmall, boardChecker.validateSize(1))
        assertEquals(BoardError.SizeTooSmall, boardChecker.validateSize(2))
        assertEquals(BoardError.SizeTooSmall, boardChecker.validateSize(3))
    }

    @Test
    fun `size greater or equal 21 returns SizeTooBig`() {
        assertEquals(BoardError.SizeTooBig, boardChecker.validateSize(21))
        assertEquals(BoardError.SizeTooBig, boardChecker.validateSize(50))
    }
}