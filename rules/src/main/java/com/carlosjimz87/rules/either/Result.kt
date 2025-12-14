package com.carlosjimz87.rules.either

sealed class Result<out V, out E> {
    data class Ok<out V>(val value: V) : Result<V, Nothing>()
    data class Err<out E>(val error: E) : Result<Nothing, E>() // Semicolon is optional

    fun getOrNull(): V? = when (this) {
        is Ok -> value
        is Err -> null
    }

    fun getErrorOrNull(): E? = when (this) {
        is Ok -> null
        is Err -> error
    }
}
