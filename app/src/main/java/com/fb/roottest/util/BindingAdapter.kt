package com.fb.roottest.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.BindingAdapter
import com.fb.roottest.home.TextInputState
import com.google.android.material.textfield.TextInputLayout

@BindingAdapter("inputState")
fun TextInputLayout.setInputState(state: TextInputState?) {
    error = state?.errorMessage?.run {
        context.getString(this)
    }


}