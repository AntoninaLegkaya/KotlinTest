package com.fb.roottest.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.fb.roottest.data.ResultQuery
import com.fb.roottest.data.repository.Repository

open class BaseViewModel(application: Application, protected val repository: Repository) : AndroidViewModel(application) {


    inner class DefaultObserver<E : Any?, T : ResultQuery<E>> : Observer<T> {
        protected val mutableIsProcessing = SingleLiveEvent<Boolean>()

        val isProcessing: LiveData<Boolean?> = mutableIsProcessing

        private var onError: (errorResult: ResultQuery.ErrorResult) -> Unit = {
            handleProcessing(false)
        }
        private var onSuccess: (successResult: ResultQuery.SuccessResult<E>) -> Unit = {
            handleProcessing(false)
        }
        private var onProcessing: (processingResult: ResultQuery.Processing) -> Unit = {
            handleProcessing(true)
        }

        private var shouldHandleProcessing: Boolean = true

        private fun handleProcessing(isProcessing: Boolean) {
            if (shouldHandleProcessing) {
                mutableIsProcessing.postValue(isProcessing)
            }
        }

        override fun onChanged(t: T) {
            when (t) {
                is ResultQuery.SuccessResult<*> -> onSuccess(t as ResultQuery.SuccessResult<E>)
                is ResultQuery.Processing -> onProcessing(t)
                is ResultQuery.ErrorResult -> onError(t)
            }
        }

        fun handleProcessing(
            withDefault: Boolean = true,
            handler: (processinResult: ResultQuery.Processing) -> Unit
        ): DefaultObserver<E, T> {
            onProcessing = if (withDefault) {
                val default = onProcessing
                {
                    default(it)
                    handler(it)
                }
            } else {
                handler
            }
            return this
        }
        fun handleError(
            withDefault: Boolean = true,
            handler: (errorResult: ResultQuery.ErrorResult) -> Unit
        ): DefaultObserver<E, T> {
            onError = if (withDefault) {
                val default = onError
                {
                    default(it)
                    handler(it)
                }
            } else {
                handler
            }
            return this
        }

        fun handleSuccess(
            withDefault: Boolean = true,
            handler: (successResult: ResultQuery.SuccessResult<E>) -> Unit
        ): DefaultObserver<E, T> {
            onSuccess = if (withDefault) {
                val default = onSuccess
                {
                    default(it)
                    handler(it)
                }
            } else {
                handler
            }
            return this
        }

    }


}

