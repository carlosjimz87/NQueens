package com.carlosjimz87.rules.solver

import com.carlosjimz87.rules.model.BoardError
import com.carlosjimz87.rules.model.Cell
import com.carlosjimz87.rules.model.GameState
import com.carlosjimz87.rules.either.Result

interface NQueensSolver {
    fun setBoardSize(size: Int): Result<GameState, BoardError>
    fun placeQueen(cell: Cell): GameState
    fun removeQueen(cell: Cell): GameState
}
