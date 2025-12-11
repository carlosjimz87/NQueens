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

class BoardViewModel : ViewModel() {

    private val _currentSize = MutableStateFlow(Constants.DEFAULT_COLUMNS_ROWS)
    val currentSize: StateFlow<Int> = _currentSize

    private val _lastValidSize = MutableStateFlow(_currentSize.value)
    val lastValidSize: StateFlow<Int> = _lastValidSize

    private val _errors = MutableSharedFlow<UiState.BoardInvalid>()
    val errors: SharedFlow<UiState.BoardInvalid> = _errors

    fun onSizeChanged(newSize: Int) {
        _currentSize.value = newSize

        when (val validation = validateNQueensBoardSize(newSize)) {
            BoardError.NoError -> {
                _lastValidSize.value = newSize
            }
            else -> {
                viewModelScope.launch {
                    _errors.emit(
                        UiState.BoardInvalid(
                            message = "Invalid size: $newSize",
                            error = validation
                        )
                    )
                }
            }
        }
    }
}