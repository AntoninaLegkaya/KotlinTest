package com.fb.roottest.base.paralax

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import java.lang.ref.WeakReference


class BackdropBottomSheetBehavior<V : View>(context: Context, attrs: AttributeSet) :
    CoordinatorLayout.Behavior<V>(context, attrs) {
    /**
     * To avoid using multiple "peekheight=" in XML and looking flexibility allowing [BottomSheetBehaviorGoogleMapsLike.mPeekHeight]
     * get changed dynamically we get the [NestedScrollView] that has
     * "app:layout_behavior=" [BottomSheetBehaviorGoogleMapsLike] inside the [CoordinatorLayout]
     */
    private var mBottomSheetBehaviorRef: WeakReference<BottomSheetBehaviorGoogleMapsLike<*>>? = null
    /**
     * Following [.onDependentViewChanged]'s docs mCurrentChildY just save the child Y
     * position.
     */
    private var mCurrentChildY: Int = 0

    override fun layoutDependsOn(parent: CoordinatorLayout, child: V, dependency: View): Boolean {
        if (dependency is NestedScrollView) {
            try {
                BottomSheetBehaviorGoogleMapsLike.from(dependency)
                return true
            } catch (e: IllegalArgumentException) {
            }

        }
        return false
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: V, dependency: View): Boolean {
        /**
         * collapsedY and achorPointY are calculated every time looking for
         * flexibility, in case that dependency's height, child's height or [BottomSheetBehaviorGoogleMapsLike.getPeekHeight]'s
         * value changes throught the time, I mean, you can have a [android.widget.ImageView]
         * using images with different sizes and you don't want to resize them or so
         */
        if (mBottomSheetBehaviorRef == null || mBottomSheetBehaviorRef!!.get() == null)
            getBottomSheetBehavior(parent)
        /**
         * mCollapsedY: Y position in where backdrop get hidden behind dependency.
         * [BottomSheetBehaviorGoogleMapsLike.getPeekHeight] and collapsedY are the same point on screen.
         */
        val collapsedY = dependency.getHeight() - mBottomSheetBehaviorRef!!.get()?.peekHeight!!
        /**
         * achorPointY: with top being Y=0, achorPointY defines the point in Y where could
         * happen 2 things:
         * The backdrop should be moved behind dependency view (when [.mCurrentChildY] got
         * positive values) or the dependency view overlaps the backdrop (when
         * [.mCurrentChildY] got negative values)
         */
        val achorPointY = child.getHeight()
        /**
         * lastCurrentChildY: Just to know if we need to return true or false at the end of this
         * method.
         */
        val lastCurrentChildY = mCurrentChildY
        mCurrentChildY = ((dependency.getY() - achorPointY) * collapsedY / (collapsedY - achorPointY)).toInt()
        if (mCurrentChildY <= 0) {
            mCurrentChildY = 0
            child.y = mCurrentChildY.toFloat()
        } else
            child.y = mCurrentChildY.toFloat()
        return lastCurrentChildY == mCurrentChildY
    }


    /**
     * Look into the CoordiantorLayout for the [BottomSheetBehaviorGoogleMapsLike]
     * @param coordinatorLayout with app:layout_behavior= [BottomSheetBehaviorGoogleMapsLike]
     */
    private fun getBottomSheetBehavior(coordinatorLayout: CoordinatorLayout) {

        for (i in 0 until coordinatorLayout.childCount) {
            val child = coordinatorLayout.getChildAt(i)

            if (child is NestedScrollView) {

                try {
                    val temp = BottomSheetBehaviorGoogleMapsLike.from(child)
                    mBottomSheetBehaviorRef = WeakReference(temp)
                    break
                } catch (e: IllegalArgumentException) {
                }

            }
        }
    }
}