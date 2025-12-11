package com.carlosjimz87.nqueens.presentation.board.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosjimz87.nqueens.common.Constants
import com.carlosjimz87.nqueens.common.validateNQueensBoardSize
import com.carlosjimz87.nqueens.domain.error.BoardError
import com.carlosjimz87.nqueens.domain.model.Cell
import com.carlosjimz87.nqueens.presentation.board.event.UiEvent
import com.carlosjimz87.nqueens.presentation.board.state.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BoardViewModel(
    initialSize: Int = Constants.DEFAULT_COLUMNS_ROWS
) : ViewModel() {

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

    init {
        applySize(initialSize)
    }

    fun onSizeChanged(newSize: Int) {
        applySize(newSize)
    }


    fun onCellClicked(cell: Cell) {
        val current = _queens.value
        if (cell in current) {
            // remove queen
            _queens.value = current - cell
        } else {
            // add queen and emit sound event
            _queens.value = current + cell
            viewModelScope.launch {
                _events.emit(UiEvent.QueenPlaced)
            }
        }
    }

    private fun applySize(size: Int) {
        if (_boardSize.value == size) return

        viewModelScope.launch {
            _uiState.value = UiState.Loading

            delay(150)
            when (val validation = validateNQueensBoardSize(size)) {
                BoardError.NoError -> {
                    _boardSize.value = size
                    _uiState.value = UiState.Idle
                    _queens.value = emptySet()
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
}