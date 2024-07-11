package com.example.starterapplication.core.knox.api

import android.util.Log
import com.example.starterapplication.core.knox.api.internals.DefaultApiError
import com.example.starterapplication.core.knox.usecase.ApiUseCaseExecutor
import com.example.starterapplication.core.knox.usecase.collectResultsOfType
import com.example.starterapplication.core.knox.usecase.executeAndMap
import com.example.starterapplication.core.knox.usecase.executeForSuccess
import com.example.starterapplication.core.knox.usecase.executeIf
import com.example.starterapplication.core.knox.usecase.executeParallel
import com.example.starterapplication.core.knox.usecase.executeWithCustomRetry
import com.example.starterapplication.core.knox.usecase.executeWithErrorHandler
import com.example.starterapplication.core.knox.usecase.executeWithFallback
import com.example.starterapplication.core.knox.usecase.executeWithLogging
import com.example.starterapplication.core.knox.usecase.executeWithRetry
import com.example.starterapplication.core.knox.usecase.executeWithTimeout
import com.example.starterapplication.core.knox.usecase.useCaseBlock
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


class UseCaseExecutorTest {

    private lateinit var executor: ApiUseCaseExecutor

    @Before
    fun setup() {
        executor = ApiUseCaseExecutor()
    }

    @Test
    fun `execute stores and returns result`() = runTest {
        // Given
        val executor = ApiUseCaseExecutor()
        val mockUseCase: suspend () -> ApiResult<String> = mockk {
            coEvery { this@mockk.invoke() } returns ApiResult.Success("Test Result")
        }

        // When
        val result = executor.execute(mockUseCase)

        // Then
        assertTrue(result is ApiResult.Success)
        assertEquals("Test Result", (result as ApiResult.Success).data)
        assertTrue(executor.allSuccessful())
        coVerify { mockUseCase.invoke() }
    }

    @Test
    fun `allSuccessful returns false when there's an error`() = runTest {
        // Given
        val successUseCase: suspend () -> ApiResult<String> = { ApiResult.Success("Success") }
        val errorUseCase: suspend () -> ApiResult<String> = { ApiResult.Error() }

        // When
        executor.execute(successUseCase)
        executor.execute(errorUseCase)

        // Then
        assertFalse(executor.allSuccessful())
    }

    @Test
    fun `errors returns list of errors`() = runTest {
        // Given
        val successUseCase: suspend () -> ApiResult<String> = { ApiResult.Success("Success") }
        val errorUseCase: suspend () -> ApiResult<String> = { ApiResult.Error(
            apiError = DefaultApiError.UnexpectedError()
            ) }

        // When
        executor.execute(successUseCase)
        executor.execute(errorUseCase)

        // Then
        assertEquals(1, executor.errors().size)
        assertTrue(executor.errors().first().apiError is DefaultApiError.UnexpectedError)
        assertEquals("An unexpected error occurred",
            executor.errors().first().apiError?.message)
    }

    @Test
    fun `getResult returns correct result by index`() = runTest {
        // Given
        val useCase1: suspend () -> ApiResult<String> = { ApiResult.Success("Result 1") }
        val useCase2: suspend () -> ApiResult<Int> = { ApiResult.Success(42) }

        // When
        executor.execute(useCase1)
        executor.execute(useCase2)

        // Then
        assertEquals("Result 1", executor.getResult<String>(0))
        assertEquals(42, executor.getResult<Int>(1))
    }

    @Test
    fun `reset clears all results`() = runTest {
        // Given
        val useCase: suspend () -> ApiResult<String> = { ApiResult.Success("Test") }
        executor.execute(useCase)

        // When
        executor.reset()

        // Then
        assertTrue(executor.getResults().isEmpty())
    }

    @Test
    fun `executeForSuccess returns data for successful call`() = runTest {
        val result = executor.executeForSuccess { ApiResult.Success("Test Data") }
        assertEquals("Test Data", result)
    }

    @Test
    fun `executeForSuccess returns null for error call`() = runTest {
        val result = executor.executeForSuccess<String> { ApiResult.Error() }
        assertNull(result)
    }

    @Test
    fun `executeWithRetry retries specified number of times`() = runTest {
        var attempts = 0
        val result = executor.executeWithRetry(times = 3) {
            attempts++
            if (attempts < 3) ApiResult.Error()
            else ApiResult.Success("Success after retry")
        }
        assertEquals(3, attempts)
        assertTrue(result is ApiResult.Success)
        assertEquals("Success after retry", (result as ApiResult.Success).data)
    }

    @Test
    fun `executeWithRetry returns error when all attempts fail`() = runTest {
        var attempts = 0
        val result = executor.executeWithRetry(times = 3) {
            attempts++
            ApiResult.Error()
        }
        assertEquals(3, attempts)
        assertTrue(result is ApiResult.Error)
    }

    @Test
    fun `executeWithRetry succeeds on first attempt`() = runTest {
        var attempts = 0
        val result = executor.executeWithRetry(times = 3) {
            attempts++
            ApiResult.Success("First Try Success")
        }
        assertEquals(1, attempts)
        assertTrue(result is ApiResult.Success)
        assertEquals("First Try Success", (result as ApiResult.Success).data)
    }

    @Test
    fun `executeWithRetry respects custom number of retry attempts`() = runTest {
        var attempts = 0
        val result = executor.executeWithRetry(times = 5) {
            attempts++
            if (attempts < 5) ApiResult.Error()
            else ApiResult.Success("Success after 4 retries")
        }
        assertEquals(5, attempts)
        assertTrue(result is ApiResult.Success)
        assertEquals("Success after 4 retries", (result as ApiResult.Success).data)
    }

    @Test
    fun `executeWithErrorHandler calls error handler on error`() = runTest {
        var errorHandled = false
        val result = executor.executeWithErrorHandler<String>(
            useCase = { ApiResult.Error() },
            errorHandler = { errorHandled = true }
        )
        assertNull(result)
        assertTrue(errorHandled)
    }

    @Test
    fun `executeIf executes use case when condition is true`() = runTest {
        val result = executor.executeIf(true) { ApiResult.Success("Executed") }
        assertTrue(result is ApiResult.Success)
    }

    @Test
    fun `executeIf does not execute use case when condition is false`() = runTest {
        val result = executor.executeIf(false) { ApiResult.Success("Not Executed") }
        assertNull(result)
    }

    @Test
    fun `executeAndMap successfully maps result`() = runTest {
        val result = executor.executeAndMap(
            useCase = { ApiResult.Success(5) },
            mapper = { it * 2 }
        )
        assertEquals(10, result)
    }

    @Test
    fun `executeWithTimeout returns result within timeout`() = runTest {
        val result = executor.executeWithTimeout(1000) {
            delay(500)
            ApiResult.Success("On Time")
        }
        assertTrue(result is ApiResult.Success)
    }

    @Test
    fun `executeWithTimeout returns error on timeout`() = runTest {
        val result = executor.executeWithTimeout(500) {
            delay(1000)
            ApiResult.Success("Too Late")
        }
        assertTrue(result is ApiResult.Error)
    }

    @Test
    fun `collectResultsOfType returns correct results`() = runTest {
        executor.execute { ApiResult.Success("String") }
        executor.execute { ApiResult.Success(42) }
        executor.execute { ApiResult.Error() }

        val stringResults = executor.collectResultsOfType<String>()
        val intResults = executor.collectResultsOfType<Int>()

        assertEquals(listOf("String"), stringResults)
        assertEquals(listOf(42), intResults)
    }

    @Test
    fun `executeParallel executes use cases concurrently`() = runTest {
        val useCase1: suspend () -> ApiResult<Unit> = mockk {
            coEvery { this@mockk.invoke() } coAnswers {
                delay(100)
                ApiResult.Success(Unit)
            }
        }
        val useCase2: suspend () -> ApiResult<Unit> = mockk {
            coEvery { this@mockk.invoke() } coAnswers {
                delay(50)
                ApiResult.Success(Unit)
            }
        }

        executor.executeParallel(useCase1, useCase2)

        assertEquals(2, executor.getResults().size)
        coVerify {
            useCase1.invoke()
            useCase2.invoke()
        }
    }

    @Test
    fun `executeWithCustomRetry retries based on predicate`() = runTest {
        val useCase: suspend () -> ApiResult<String> = mockk()
        coEvery { useCase.invoke() } returnsMany listOf(
            ApiResult.Error(apiError = DefaultApiError.UnexpectedError()),
            ApiResult.Error(apiError = DefaultApiError.UnexpectedError()),
            ApiResult.Success("Success")
        )

        val result = executor.executeWithCustomRetry(
            useCase = useCase,
            retryPredicate = { it.apiError is DefaultApiError.UnexpectedError },
            maxAttempts = 5
        )

        assertTrue(result is ApiResult.Success)
        assertEquals("Success", (result as ApiResult.Success).data)
        coVerify(exactly = 3) { useCase.invoke() }
    }

    @Test
    fun `executeWithLogging logs execution`() = runTest {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        val result = executor.executeWithLogging("TestTag") {
            ApiResult.Success("Logged Result")
        }

        assertTrue(result is ApiResult.Success)
        verify(exactly = 2) { Log.d("TestTag", any()) }
    }

    @Test
    fun `executeWithFallback uses fallback when primary fails`() = runTest {
        val primaryUseCase: suspend () -> ApiResult<String> = mockk {
            coEvery { this@mockk.invoke() } returns ApiResult.Error()
        }
        val fallbackUseCase: suspend () -> ApiResult<String> = mockk {
            coEvery { this@mockk.invoke() } returns ApiResult.Success("Fallback Success")
        }

        val result = executor.executeWithFallback(
            primaryUseCase = primaryUseCase,
            fallbackUseCase = fallbackUseCase
        )

        assertTrue(result is ApiResult.Success)
        assertEquals("Fallback Success", (result as ApiResult.Success).data)
        coVerify {
            primaryUseCase.invoke()
            fallbackUseCase.invoke()
        }
    }

    @Test
    fun `useCaseBlock creates executor, executes block, and returns executor with results`() = runTest {
        val result = useCaseBlock {
            execute { ApiResult.Success("First call") }
            execute { ApiResult.Error(apiError = DefaultApiError.UnexpectedError()) }
            execute { ApiResult.Success(42) }
        }

        // Check that the executor has the correct number of results
        assertEquals(3, result.getResults().size)

        // Check the types and values of the results
        val results = result.getResults()
        assertTrue(results[0] is ApiResult.Success)
        assertEquals("First call", (results[0] as ApiResult.Success).data)

        assertTrue(results[1] is ApiResult.Error)
        assertTrue((results[1] as ApiResult.Error).apiError is DefaultApiError.UnexpectedError)
        assertEquals("An unexpected error occurred", (results[1] as ApiResult.Error).apiError?.message)

        assertTrue(results[2] is ApiResult.Success)
        assertEquals(42, (results[2] as ApiResult.Success).data)

        // Check that allSuccessful returns false (because of the error)
        assertFalse(result.allSuccessful())

        // Check that errors returns one error
        assertEquals(1, result.errors().size)
    }
}