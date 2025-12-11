package com.carlosjimz87.nqueens.common

import com.carlosjimz87.nqueens.domain.error.BoardError
import org.junit.Assert.assertEquals
import org.junit.Test

class ValidateNQueensBoardSizeTest {
    @Test
    fun `size 4 to 20 returns NoError`() {
        for (n in 4..20) {
            assertEquals(BoardError.NoError, validateNQueensBoardSize(n))
        }
    }

    @Test
    fun `size less or equal 3 returns SizeTooSmall`() {
        assertEquals(BoardError.SizeTooSmall, validateNQueensBoardSize(0))
        assertEquals(BoardError.SizeTooSmall, validateNQueensBoardSize(1))
        assertEquals(BoardError.SizeTooSmall, validateNQueensBoardSize(2))
        assertEquals(BoardError.SizeTooSmall, validateNQueensBoardSize(3))
    }

    @Test
    fun `size greater or equal 21 returns SizeTooBig`() {
        assertEquals(BoardError.SizeTooBig, validateNQueensBoardSize(21))
        assertEquals(BoardError.SizeTooBig, validateNQueensBoardSize(50))
    }
}