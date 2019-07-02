package com.fb.roottest.data

import kotlinx.coroutines.Job

sealed class ResultQuery<out T : Any?>(protected val resultData: T?) {

    class SuccessResult<out T : Any?>(result: T) : ResultQuery<T>(result) {
        fun getResult() = resultData
    }

    class Processing(private val coroutineScope: Job?) : ResultQuery<Nothing>(null) {
        fun cancelOperation() {
            coroutineScope?.cancel()
        }

        fun map(): Processing {
            return Processing(coroutineScope)
        }
    }

    open class ErrorResult(open val t: Throwable) : ResultQuery<Nothing>(null) {

        open fun map(): ErrorResult {
            return ErrorResult(t)
        }
    }
}

suspend fun <T : Any> ResultQuery<T>.applyToSuccess(
    function: suspend (T) -> Unit
): ResultQuery<T> = when (this) {

    is ResultQuery.SuccessResult -> {
        getResult()?.let {
            function(it)
        }
        this
    }
    else -> this
}

suspend fun <T : Any, R : Any> ResultQuery<T>.mapSuccess(
    mapFunc: suspend (T) -> ResultQuery<R>
): ResultQuery<R>? = when (this) {
    is ResultQuery.SuccessResult -> {
        getResult()?.let {
            mapFunc(it)
        }
    }
    is ResultQuery.ErrorResult -> map()
    is ResultQuery.Processing -> null

}