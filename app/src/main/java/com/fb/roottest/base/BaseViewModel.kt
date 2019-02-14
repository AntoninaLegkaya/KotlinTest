package com.fb.roottest.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel

open class BaseViewModel(application: Application): AndroidViewModel(application){

    protected val _connectionError= SingleLiveEvent<Unit>()
    protected val _isProcessing= SingleLiveEvent<Boolean>()




}