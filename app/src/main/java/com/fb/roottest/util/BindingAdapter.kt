package com.fb.roottest.util

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.fb.roottest.home.TextInputState
import com.google.android.material.textfield.TextInputLayout

@BindingAdapter("inputState")
fun TextInputLayout.setInputState(state: TextInputState?) {
    error = state?.errorMessage?.run {
        context.getString(this)
    }
}

    @BindingAdapter("srcBase64")
    fun ImageView.setSrcBase64(srcBase64: String) {
        srcBase64.let {
            val decodedString =  Base64.decode(it, Base64.DEFAULT)
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

            Glide.with(this)
                .asBitmap()
                .load(decodedByte)
                .into(this)
        }
    }

