package com.fb.roottest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel(), ToolbarHandler {
    private val _visibleToolbar = MutableLiveData<Boolean>()
    private val _title = MutableLiveData<String>()

    val toolbarVisible: LiveData<Boolean>
        get() = _visibleToolbar
    val toolbarTitle: LiveData<String>
        get() = _title

    override fun setTitle(title: String?) {
        _title.value = title ?: ""
    }

    override fun visibleToolbar(visible: Boolean) {
        _visibleToolbar.value = visible
    }
}

interface ToolbarHandler {
    fun setTitle(title: String?)
    fun visibleToolbar(visible: Boolean)
}
