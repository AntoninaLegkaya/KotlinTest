package com.fb.roottest.base.paralax

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior

class ExpandPanelBehavior<V : View>(context: Context?, attrs: AttributeSet?) : BottomSheetBehavior<V>(context, attrs) {
    companion object {
        fun <V : View> from(view: V): ExpandPanelBehavior<V> {
            val params = view.layoutParams
            if (params !is androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams) {
                throw IllegalArgumentException("The view is not a child of CoordinatorLayout") as Throwable
            } else {
                val behavior = params.behavior as ExpandPanelBehavior
                return behavior as? ExpandPanelBehavior<V>
                    ?: throw IllegalArgumentException("The view is not associated with ExpandPanelBehavior")
            }
        }
    }
}