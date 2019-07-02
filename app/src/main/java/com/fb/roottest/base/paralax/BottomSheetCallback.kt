package com.fb.roottest.base.paralax

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior

open class BottomSheetCallback(private val handler: BottomSheetStateListener) :
    BottomSheetBehavior.BottomSheetCallback() {

    override fun onSlide(bottomSheet: View, slideOffset: Float) {
        // Nothing impl

    }

    override fun onStateChanged(bottomSheet: View, newState: Int) {
        when (newState) {
            BottomSheetBehavior.STATE_COLLAPSED -> {
                handler.sheetCollapse()
            }
            BottomSheetBehavior.STATE_EXPANDED -> {
                handler.sheetExpand()
            }
        }
    }
}