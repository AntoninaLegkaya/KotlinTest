package com.fb.roottest.base.paralax

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import com.fb.roottest.custom.MergedAppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.lang.ref.WeakReference


class ScrollAwareFABBehavior(context: Context, attrs: AttributeSet) : FloatingActionButton.Behavior() {

    /**
     * One of the point used to set hide() or show() in FAB
     */
    private var offset: Float = 0.toFloat()
    /**
     * The FAB should be hidden when it reach [.offset] or when [BottomSheetBehaviorGoogleMapsLike]
     * is visually lower than [BottomSheetBehaviorGoogleMapsLike.getPeekHeight].
     * We got a reference to the object to allow change dynamically PeekHeight in BottomSheet and
     * got updated here.
     */
    private var mBottomSheetBehaviorRef: WeakReference<BottomSheetBehaviorGoogleMapsLike<*>>? = null

    init {
        offset = 0f
        mBottomSheetBehaviorRef = null
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout, child: FloatingActionButton,
        directTargetChild: View, target: View, nestedScrollAxes: Int
    ): Boolean {
        //         Ensure we react to vertical scrolling
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: FloatingActionButton, dependency: View): Boolean {
        if (dependency is NestedScrollView) {
            try {
                BottomSheetBehaviorGoogleMapsLike.from<View>(dependency)
                return true
            } catch (e: IllegalArgumentException) {
            }

        }
        return false
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: FloatingActionButton,
        dependency: View
    ): Boolean {
        /**
         * Because we are not moving it, we always return false in this method.
         */

        if (offset == 0f)
            setOffsetValue(parent)

        if (mBottomSheetBehaviorRef == null)
            getBottomSheetBehavior(parent)

        val DyFix = getDyBetweenChildAndDependency(child, dependency)

        if (child.y + DyFix < offset)
            child.hide()
        else if (child.y + DyFix >= offset) {

            /**
             * We are calculating every time point in Y where BottomSheet get [BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED].
             * If PeekHeight change dynamically we can reflect the behavior asap.
             */
            if (mBottomSheetBehaviorRef == null || mBottomSheetBehaviorRef!!.get() == null)
                getBottomSheetBehavior(parent)
            val collapsedY = dependency.getHeight() - mBottomSheetBehaviorRef!!.get()!!.peekHeight

            if (child.y + DyFix > collapsedY)
                child.hide()
            else
                child.show()
        }

        return false
    }

    /**
     * In some <bold>WEIRD</bold> cases, mostly when you perform a little scroll but a fast one
     * the [.onDependentViewChanged] DOESN'T
     * reflect the real Y position of child mean the dependency get a better APROXIMATION of the real
     * Y. This was causing that FAB some times doesn't get unhidden.
     * @param child the FAB
     * @param dependency NestedScrollView instance
     * @return Dy betweens those 2 elements in Y, minus child's height/2
     */
    private fun getDyBetweenChildAndDependency(child: FloatingActionButton, dependency: View): Int {
        if (dependency.y.toInt() == 0 || dependency.getY() < offset)
            return 0

        return if (dependency.getY() - child.y > child.height)
            Math.max(0, (dependency.getY() - child.height / 2 - child.y).toInt())
        else
            0
    }

    /**
     * Define one of the point in where the FAB should be hide when it reaches that point.
     * @param coordinatorLayout container of BottomSheet and AppBarLayout
     */
    private fun setOffsetValue(coordinatorLayout: CoordinatorLayout) {

        for (i in 0 until coordinatorLayout.childCount) {
            val child = coordinatorLayout.getChildAt(i)

            if (child is MergedAppBarLayout) {
                offset = child.getY() + child.getHeight()
                break
            }

        }
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
                    val temp = BottomSheetBehaviorGoogleMapsLike.from<View>(child)
                    mBottomSheetBehaviorRef = WeakReference(temp)
                    break
                } catch (e: IllegalArgumentException) {
                }

            }
        }
    }
}