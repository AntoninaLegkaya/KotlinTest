package com.fb.roottest.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.AppBarLayout
import androidx.core.content.ContextCompat
import com.fb.roottest.R


class MergedAppBarLayout : AppBarLayout {

      lateinit  var toolbar: Toolbar
     lateinit var background: View

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    private fun init() {
        View.inflate(context, R.layout.mergedappbarlayout, this)
        //to avoid expose xml attributes to the final programmer user, I added some of them here
        setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        context.setTheme(R.style.AppTheme_AppBarOverlay)

        toolbar = findViewById(R.id.expanded_toolbar)
        background = findViewById(R.id.background)
    }
}