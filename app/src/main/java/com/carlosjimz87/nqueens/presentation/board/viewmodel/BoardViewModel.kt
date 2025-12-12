package com.carlosjimz87.nqueens.presentation.board.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosjimz87.nqueens.common.Constants
import com.carlosjimz87.nqueens.common.validateNQueensBoardSize
import com.carlosjimz87.nqueens.domain.error.BoardError
import com.carlosjimz87.nqueens.domain.model.Cell
import com.carlosjimz87.nqueens.domain.model.GameStatus
import com.carlosjimz87.nqueens.presentation.board.event.UiEvent
import com.carlosjimz87.nqueens.presentation.board.state.UiState
import com.carlosjimz87.nqueens.presentation.timer.GameTimer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BoardViewModel(
    private val timer: GameTimer
): ViewModel() {

    private val _boardSize = MutableStateFlow<Int?>(null)
    val boardSize: StateFlow<Int?> = _boardSize

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    private val _queens = MutableStateFlow<Set<Cell>>(emptySet())
    val queens: StateFlow<Set<Cell>> = _queens

    private val _events = MutableSharedFlow<UiEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val events: SharedFlow<UiEvent> = _events

    private val _gameStatus = MutableStateFlow<GameStatus?>(null)
    val gameStatus: StateFlow<GameStatus?> = _gameStatus

    val elapsedMillis: StateFlow<Long> = timer.elapsedMillis

    private var movesCount: Int = 0

    init {
        applySize(Constants.DEFAULT_COLUMNS_ROWS)
    }

    fun onSizeChanged(newSize: Int) {
        applySize(newSize)
    }

    fun onCellClicked(cell: Cell) {
        val current = _queens.value
        val isPlacing = cell !in current

        if (current.isEmpty() && isPlacing) {
            timer.start()
        }

        movesCount++

        _queens.value = if (isPlacing) {
            current + cell
        } else {
            current - cell
        }

        viewModelScope.launch {
            _events.emit(
                if (isPlacing) UiEvent.QueenPlaced(cell) else UiEvent.QueenRemoved(cell)
            )
        }

        updateGameStatus()
    }

    private fun applySize(size: Int) {
        if (_boardSize.value == size) return

        viewModelScope.launch {
            _uiState.value = UiState.Loading

            delay(150)
            when (val validation = validateNQueensBoardSize(size)) {
                BoardError.NoError -> {
                    _boardSize.value = size
                    _queens.value = emptySet()
                    movesCount = 0
                    timer.reset()
                    _uiState.value = UiState.Idle
                    _gameStatus.value = GameStatus.NotStarted(size)
                    _events.emit(
                        UiEvent.BoardReset
                    )
                }
                else -> {
                    _uiState.value = UiState.BoardInvalid(
                        message = "Invalid size: $size",
                        error = validation
                    )
                }
            }
        }
    }

    private fun updateGameStatus() {
        val size = _boardSize.value ?: return
        val queensCount = _queens.value.size

        // TODO: replace 0 with real conflicts count when we implement rules
        val conflicts = 0

        _gameStatus.value = when {
            queensCount == 0 -> {
                timer.reset()
                GameStatus.NotStarted(size)
            }
            queensCount == size && conflicts == 0 -> {
                timer.stop()
                GameStatus.Solved(
                    size = size,
                    moves = movesCount
                )
            }
            else -> GameStatus.InProgress(
                size = size,
                queensPlaced = queensCount,
                conflicts = conflicts
            )
        }
    }
}