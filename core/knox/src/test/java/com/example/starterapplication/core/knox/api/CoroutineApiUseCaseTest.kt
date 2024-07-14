package com.example.starterapplication.core.knox.api

import com.example.starterapplication.core.knox.api.internals.DefaultApiError
import com.example.starterapplication.core.knox.usecase.CoroutineApiUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoroutineApiUseCaseTest : BaseCoroutineTest() {

    @Test
    fun `execute returns success when no exception is thrown`() = runTest(testDispatcher) {
        // Given
        val useCase = object : CoroutineApiUseCase<Unit, String>(testDispatcher) {
            override suspend fun execute(params: Unit): ApiResult<String> {
                return ApiResult.Success("Test Result")
            }
        }

        // When
        val result = useCase(Unit)

        // Then
        assertTrue(result is ApiResult.Success)
        assertEquals("Test Result", (result as ApiResult.Success).data)
    }

    @Test
    fun `execute returns error when exception is thrown`() = runTest(testDispatcher) {
        // Given
        val useCase = object : CoroutineApiUseCase<Unit, String>(testDispatcher) {
            override suspend fun execute(params: Unit): ApiResult<String> {
                throw RuntimeException("Test Exception")
            }
        }

        // When
        val result = useCase(Unit)

        // Then
        assertTrue(result is ApiResult.Error)
        assertEquals((result as ApiResult.Error).apiError?.message, "Test Exception")
    }

    @Test
    fun `execute returns not supported for NoSuchMethodError`() = runTest(testDispatcher) {
        // Given
        val useCase = object : CoroutineApiUseCase<Unit, String>(testDispatcher) {
            override suspend fun execute(params: Unit): ApiResult<String> {
                throw NoSuchMethodError("Test Method Error")
            }
        }

        // When
        val result = useCase(Unit)

        // Then
        assertTrue(result is ApiResult.NotSupported)
    }

    @Test
    fun `execute returns security error for SecurityException`() = runTest(testDispatcher) {
        // Given
        val useCase = object : CoroutineApiUseCase<Unit, String>(testDispatcher) {
            override suspend fun execute(params: Unit): ApiResult<String> {
                throw SecurityException("Test Security Exception")
            }
        }

        // When
        val result = useCase(Unit)

        // Then
        assertTrue(result is ApiResult.Error)
        assertTrue((result as ApiResult.Error).apiError is DefaultApiError.PermissionError)
        assertTrue(result.exception is SecurityException)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun `execute uses provided dispatcher`() = runTest {
        // Given
        var dispatcherUsed = false
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val useCase = object : CoroutineApiUseCase<Unit, String>(testDispatcher) {
            override suspend fun execute(params: Unit): ApiResult<String> {
                dispatcherUsed = currentCoroutineContext()[CoroutineDispatcher] == testDispatcher
                return ApiResult.Success("Test Result")
            }
        }

        // When
        useCase(Unit)

        // Then
        assertTrue("The provided dispatcher was not used", dispatcherUsed)
    }
}