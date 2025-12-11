package com.carlosjimz87.nqueens.presentation.board.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosjimz87.nqueens.common.Constants
import com.carlosjimz87.nqueens.common.validateNQueensBoardSize
import com.carlosjimz87.nqueens.domain.error.BoardError
import com.carlosjimz87.nqueens.presentation.board.state.UiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BoardViewModel(
    private val initialSize: Int = Constants.DEFAULT_COLUMNS_ROWS
) : ViewModel() {

    private val _boardSize = MutableStateFlow<Int?>(null)
    val boardSize: StateFlow<Int?> = _boardSize

    private val _errors = MutableSharedFlow<UiState.BoardInvalid>()
    val errors: SharedFlow<UiState.BoardInvalid> = _errors

    init {
        applySize(initialSize)
    }

    fun onSizeChanged(newSize: Int) {
        applySize(newSize)
    }

    private fun applySize(size: Int) {
        when (val validation = validateNQueensBoardSize(size)) {
            BoardError.NoError -> {
                _boardSize.value = size
            }

            else -> {
                _boardSize.value = null
                viewModelScope.launch {
                    _errors.emit(
                        UiState.BoardInvalid(
                            message = "Invalid size: $size",
                            error = validation
                        )
                    )
                }
            }
        }
    }
}