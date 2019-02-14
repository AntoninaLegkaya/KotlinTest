package com.fb.roottest.base

import androidx.lifecycle.ViewModel
import com.fb.roottest.ToolbarHandler

class BaseFragmentViewModel : ViewModel() {

    private var toolbarHandler: ToolbarHandler? = null

    private var title: String? = null
    private var toolbarIsVisible: Boolean? = null

    fun initSetup(title: String?, toolbarIsVisible: Boolean) {
        //Setup fields if they hadn't been initialized yet (if they are null)
        if(this.title == null) {
            this.title = title ?: ""
        }
        if(this.toolbarIsVisible == null) {
            this.toolbarIsVisible = toolbarIsVisible
        }
    }

    fun start(toolbarHandler: ToolbarHandler) {
        this.toolbarHandler = toolbarHandler
    }

    fun updateTitle(title: String?) {
        this.title = title
        toolbarHandler?.setTitle(title)
    }

    fun updateToolbarVisibility(isVisible: Boolean) {
        this.toolbarIsVisible = isVisible
        toolbarHandler?.visibleToolbar(isVisible)
    }

    fun onResume() {
        toolbarHandler?.apply {
            setTitle(title)
            visibleToolbar(toolbarIsVisible ?: true)
        }
    }
}