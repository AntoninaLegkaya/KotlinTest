package com.fb.roottest.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.fb.roottest.base.ViewModelFactory

private const val OBTAIN_VIEWMODEL_EXCEPTION_MESSAGE = "Activity is null when trying to obtain viewModel"

fun <T> LifecycleOwner.observeCommand(data: LiveData<T>, action: (T) -> Unit) {
    data.observe(this, Observer(action))
}

fun <T : ViewModel> Fragment.obtainViewModel(clazz: Class<T>): T {
    return activity?.run {
        obtainViewModel(ViewModelFactory.getInstance(application), clazz)
    } ?: throw IllegalStateException(OBTAIN_VIEWMODEL_EXCEPTION_MESSAGE)
}

fun <VM : ViewModel> Fragment.obtainViewModel(factory: ViewModelProvider.Factory?, cls: Class<VM>): VM {
    return ViewModelProviders.of(this, factory).get(cls)
}

fun ViewGroup.inflateView(@LayoutRes layout: Int): View {
    return LayoutInflater.from(this.context).inflate(layout, this, false)
}