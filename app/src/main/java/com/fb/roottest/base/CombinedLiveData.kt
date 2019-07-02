package com.fb.roottest.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

/**
 * Combine two livedatas into one.
 */
fun <T, A, B> LiveData<A>.combineWith(
    other: LiveData<B>,
    combineOperation: (A?, B?) -> T
): MediatorLiveData<T> {
    var data1: A? = null
    var data2: B? = null

    return MediatorLiveData<T>().apply {
        addSource(this@combineWith) {
            data1 = it
            value = combineOperation(data1,
                data2)
        }
        addSource(other) {
            data2 = it
            value = combineOperation(data1,
                data2)
        }
    }
}

/**
 * Combine multiple live data
 */
inline fun <reified A, T> combine(
    crossinline combineOperation: (Array<A?>) -> T,
    vararg liveDataArray: LiveData<A>
): MediatorLiveData<T> {
    val valueArray = Array<A?>(liveDataArray.size) { null }

    return MediatorLiveData<T>().apply {
        liveDataArray.forEachIndexed { index, data ->
            addSource(data) {
                valueArray[index] = it
                combineOperation(valueArray)
            }
        }
    }
}
