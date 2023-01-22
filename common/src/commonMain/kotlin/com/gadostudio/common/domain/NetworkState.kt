package com.gadostudio.common.domain

sealed class NetworkState<T> {
    class Loading<T> : NetworkState<T>()
    class Loaded<T>(val data: T) : NetworkState<T>()
    class Error<T>(val error: Exception) : NetworkState<T>()
}