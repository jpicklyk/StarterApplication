package com.example.starterapplication.core.knox.usecase

import android.util.Log
import com.example.starterapplication.core.knox.api.ApiResult
import com.example.starterapplication.core.knox.api.internals.DefaultApiError
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlin.math.min

/**
 * A utility class for executing multiple use cases and managing their results.
 * This class provides a centralized way to execute API calls, handle errors, and manage results
 * across multiple use cases. It's particularly useful for complex operations that involve
 * multiple API calls or when you need to track the results of multiple operations.
 *
 * Use this class when you need to:
 * - Execute multiple use cases in sequence or parallel
 * - Implement retry logic for failed API calls
 * - Handle errors across multiple use cases
 * - Collect and analyze results from multiple operations
 */
class ApiUseCaseExecutor {
    private val results = mutableListOf<ApiResult<*>>()

    /**
     * Provides a read-only view of the results.
     *
     * @return A list of all executed ApiResult results.
     */
    fun getResults(): List<ApiResult<*>> = results.toList()

    /**
     * Executes a use case and stores its result.
     *
     * @param useCase The use case to execute.
     * @return The result of the use case execution.
     */
    suspend fun <T : Any> execute(useCase: suspend () -> ApiResult<T>): ApiResult<T> {
        val result = useCase()
        results.add(result)
        return result
    }

    /**
     * Checks if all executed use cases were successful.
     *
     * @return True if all results are [ApiResult.Success], false otherwise.
     */
    fun allSuccessful(): Boolean = results.all { it is ApiResult.Success }

    /**
     * Retrieves all error results from executed use cases.
     *
     * @return A list of [ApiResult.Error] results.
     */
    fun errors(): List<ApiResult.Error> = results.filterIsInstance<ApiResult.Error>()

    /**
     * Retrieves a specific result by index and casts it to the expected type.
     *
     * @param index The index of the result to retrieve.
     * @return The data of the successful result, or null if the result is not a success or doesn't exist.
     */
    fun <T : Any> getResult(index: Int): Any? = (results.getOrNull(index) as? ApiResult.Success)?.data

    /**
     * Clears all stored results.
     */
    fun reset() {
        results.clear()
    }
}

/**
 * Executes a block of use cases using [ApiUseCaseExecutor].
 *
 * @param block The block of code to execute within the context of [ApiUseCaseExecutor].
 * @return The [ApiUseCaseExecutor] instance after executing the block.
 */
suspend fun useCaseBlock(block: suspend ApiUseCaseExecutor.() -> Unit): ApiUseCaseExecutor {
    return ApiUseCaseExecutor().apply { block() }
}

/**
 * Executes a use case and returns its successful result, or null if not successful.
 *
 * @param useCase The use case to execute.
 * @return The data of the successful result, or null if not successful.
 */
suspend fun <T : Any> ApiUseCaseExecutor.executeForSuccess(useCase: suspend () -> ApiResult<T>): T? {
    return when (val result = execute(useCase)) {
        is ApiResult.Success -> result.data
        else -> null
    }
}

/**
 * Executes a use case with retry functionality and exponential backoff.
 *
 * This function will attempt to execute the use case up to the specified number of times.
 * If the use case results in an error, it will retry with an exponential backoff delay
 * until either a success is achieved, the maximum number of attempts is reached,
 * or a non-retryable result (like NotSupported) is returned.
 *
 * The retry behavior is as follows:
 * - On success: Returns immediately with the successful result.
 * - On NotSupported: Returns immediately with the NotSupported result.
 * - On Error: Retries the use case if there are remaining attempts, with an increasing delay.
 * - If all attempts result in Error: Returns the last error encountered.
 *
 * @param times The maximum number of execution attempts, including the initial attempt.
 * @param initialDelay The delay before the first retry attempt, in milliseconds.
 * @param maxDelay The maximum delay between retry attempts, in milliseconds.
 * @param factor The multiplier for the delay after each retry attempt.
 * @param useCase The use case to execute.
 * @return The result of the use case execution, potentially after retries.
 */
suspend fun ApiUseCaseExecutor.executeWithRetry(
    times: Int = 3,
    initialDelay: Long = 100,
    maxDelay: Long = 500,
    factor: Double = 2.0,
    useCase: suspend () -> ApiResult<*>
): ApiResult<*> {
    var currentDelay = initialDelay
    repeat(times) { attempt ->
        when (val result = execute(useCase)) {
            is ApiResult.Success -> return result
            is ApiResult.NotSupported -> return result
            is ApiResult.Error -> {
                if (attempt == times - 1) return result
                delay(currentDelay)
                currentDelay = min(currentDelay * factor, maxDelay.toDouble()).toLong()
            }
        }
    }
    // This line should never be reached, but is needed for compilation
    throw IllegalStateException("Unexpected state in executeWithRetry")
}

/**
 * Executes a use case with a custom error handler.
 *
 * @param useCase The use case to execute.
 * @param errorHandler A function to handle errors.
 * @return The data of the successful result, or null if an error occurred.
 */
suspend fun <T : Any> ApiUseCaseExecutor.executeWithErrorHandler(
    useCase: suspend () -> ApiResult<T>,
    errorHandler: (ApiResult.Error) -> Unit
): T? {
    return when (val result = execute(useCase)) {
        is ApiResult.Success -> result.data
        is ApiResult.Error -> {
            errorHandler(result)
            null
        }
        is ApiResult.NotSupported -> null
    }
}

/**
 * Conditionally executes a use case.
 *
 * @param condition The condition to check before execution.
 * @param useCase The use case to execute if the condition is true.
 * @return The result of the use case execution if condition is true, null otherwise.
 */
suspend fun <T : Any> ApiUseCaseExecutor.executeIf(
    condition: Boolean,
    useCase: suspend () -> ApiResult<T>
): ApiResult<T>? {
    return if (condition) execute(useCase) else null
}

/**
 * Executes a use case and maps its successful result.
 *
 * @param useCase The use case to execute.
 * @param mapper A function to map the successful result.
 * @return The mapped result if successful, null otherwise.
 */
suspend fun <T : Any, R : Any> ApiUseCaseExecutor.executeAndMap(
    useCase: suspend () -> ApiResult<T>,
    mapper: (T) -> R
): R? {
    return when (val result = execute(useCase)) {
        is ApiResult.Success -> mapper(result.data)
        else -> null
    }
}

/**
 * Executes a use case with a timeout.
 *
 * @param timeoutMs The timeout duration in milliseconds.
 * @param useCase The use case to execute.
 * @return The result of the use case execution, or a timeout error.
 */
suspend fun <T : Any> ApiUseCaseExecutor.executeWithTimeout(
    timeoutMs: Long,
    useCase: suspend () -> ApiResult<T>
): ApiResult<T> {
    return execute {
        try {
            withTimeout(timeoutMs) {
                useCase()
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            ApiResult.Error(
                apiError = DefaultApiError.TimeoutError(e.message ?: "Timeout exceeded"),
                exception = e
            )
        }
    }
}

/**
 * Collects all successful results of a specific type.
 *
 * @return A list of successful results cast to the specified type.
 */
inline fun <reified T : Any> ApiUseCaseExecutor.collectResultsOfType(): List<T> {
    return getResults().filterIsInstance<ApiResult.Success<*>>()
        .mapNotNull { it.data as? T }
}

/**
 * Executes multiple use cases in parallel.
 *
 * @param useCases The use cases to execute in parallel.
 */
suspend fun ApiUseCaseExecutor.executeParallel(vararg useCases: suspend () -> ApiResult<*>) {
    coroutineScope {
        useCases.map { useCase ->
            async { execute(useCase) }
        }.awaitAll()
    }
}

/**
 * Executes a use case with custom retry logic.
 *
 * This function allows for more fine-grained control over the retry behavior by using
 * a custom predicate to determine whether a retry should be attempted.
 *
 * @param useCase The use case to execute.
 * @param retryPredicate A function to determine if a retry should be attempted. It takes an
 *                       ApiResult.Error as input and returns a boolean indicating whether to retry.
 * @param maxAttempts The maximum number of attempts.
 * @return The result of the use case execution, potentially after retries.
 *
 * Example usage:
 * ```
 * executor.executeWithCustomRetry(
 *     useCase = { someApiResult() },
 *     retryPredicate = { error ->
 *         error.exception is NetworkException && error.exception.isTransient
 *     },
 *     maxAttempts = 3
 * )
 * ```
 * In this example, the use case will be retried only if the error is a transient network exception,
 * up to a maximum of 3 attempts.
 */
suspend fun <T : Any> ApiUseCaseExecutor.executeWithCustomRetry(
    useCase: suspend () -> ApiResult<T>,
    retryPredicate: (ApiResult.Error) -> Boolean,
    maxAttempts: Int = 3
): ApiResult<T> {
    repeat(maxAttempts) { attempt ->
        when (val result = execute(useCase)) {
            is ApiResult.Success -> return result
            is ApiResult.Error -> {
                if (!retryPredicate(result) || attempt == maxAttempts - 1) {
                    return result
                }
                // If not the last attempt and predicate is true, continue to next iteration
            }
            is ApiResult.NotSupported -> return result
        }
    }
    // This line should never be reached, but is needed for compilation
    throw IllegalStateException("Unexpected state in executeWithCustomRetry")
}

/**
 * Executes a use case with logging.
 *
 * @param tag The tag to use for logging.
 * @param useCase The use case to execute.
 * @return The result of the use case execution.
 */
suspend fun <T : Any> ApiUseCaseExecutor.executeWithLogging(
    tag: String,
    useCase: suspend () -> ApiResult<T>
): ApiResult<T> {
    Log.d(tag, "Executing use case")
    val result = execute(useCase)
    Log.d(tag, "Use case result: $result")
    return result
}

/**
 * Executes a primary use case with a fallback.
 *
 * @param primaryUseCase The primary use case to execute.
 * @param fallbackUseCase The fallback use case to execute if the primary fails.
 * @return The result of either the primary or fallback use case execution.
 */
suspend fun <T : Any> ApiUseCaseExecutor.executeWithFallback(
    primaryUseCase: suspend () -> ApiResult<T>,
    fallbackUseCase: suspend () -> ApiResult<T>
): ApiResult<T> {
    val primaryResult = execute(primaryUseCase)
    return if (primaryResult is ApiResult.Success) primaryResult else execute(fallbackUseCase)
}
