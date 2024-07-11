package com.example.starterapplication.core.knox.api

sealed class ApiResult<out T : Any> {
    data class Success<out T: Any>(val data: T): ApiResult<T>()
    data class Error(
        val apiError: ApiError? = null,
        val exception: Exception? = null
    ) : ApiResult<Nothing>()
    data object NotSupported: ApiResult<Nothing>()

    //Helper function to easily get the data or null
    fun getOrNull(): T? = if (this is Success) data else null

}