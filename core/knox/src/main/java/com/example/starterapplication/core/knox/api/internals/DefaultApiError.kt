package com.example.starterapplication.core.knox.api.internals

import com.example.starterapplication.core.knox.api.ApiError

sealed class DefaultApiError : ApiError {
    data class PermissionError(override val message: String) : DefaultApiError()
    data class UnexpectedError(override val message: String = "An unexpected error occurred") : DefaultApiError()
    data class TimeoutError(override val message: String) : DefaultApiError()
}