package com.fb.roottest.base.paralax

import android.animation.Animator
import android.view.View
import android.view.ViewPropertyAnimator
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import com.google.android.material.appbar.AppBarLayout
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import android.view.WindowManager
import android.app.Activity
import android.os.Build
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import com.fb.roottest.R
import java.lang.ref.WeakReference


class ScrollingAppBarLayoutBehavior(private val mContext: Context, attrs: AttributeSet) :
    AppBarLayout.ScrollingViewBehavior(mContext, attrs) {

    private var mInit = false
    private var mVisible = true
    /**
     * To avoid using multiple "peekheight=" in XML and looking flexibility allowing [BottomSheetBehaviorGoogleMapsLike.mPeekHeight]
     * get changed dynamically we get the [NestedScrollView] that has
     * "app:layout_behavior=" [BottomSheetBehaviorGoogleMapsLike] inside the [CoordinatorLayout]
     */
    private var mBottomSheetBehaviorRef: WeakReference<BottomSheetBehaviorGoogleMapsLike<*>>? = null

    private var mAppBarYValueAnimator: ValueAnimator? = null

    private val statusBarHeight: Int
        get() {
            var result = 0
            val resourceId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = mContext.getResources().getDimensionPixelSize(resourceId)
            }
            return result
        }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        if (dependency is NestedScrollView) {
            try {
                BottomSheetBehaviorGoogleMapsLike.from(dependency)
                return true
            } catch (e: IllegalArgumentException) {
            }

        }
        return false
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        if (!mInit) {
            return init(parent, child, dependency)
        }
        if (mBottomSheetBehaviorRef == null || mBottomSheetBehaviorRef!!.get() == null)
            getBottomSheetBehavior(parent)
        setAppBarVisible(
            child as AppBarLayout,
            dependency.y >= dependency.height - mBottomSheetBehaviorRef!!.get()!!.peekHeight
        )
        return true
    }

    override fun onSaveInstanceState(parent: CoordinatorLayout, child: View): Parcelable {
        return SavedState(super.onSaveInstanceState(parent, child)!!, mVisible)
    }

    override fun onRestoreInstanceState(parent: CoordinatorLayout, child: View, state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(parent, child, ss.superState)
        this.mVisible = ss.mVisible
    }

    private fun init(parent: CoordinatorLayout?, child: View, dependency: View): Boolean {
        /**
         * First we need to know if dependency view is upper or lower compared with
         * [BottomSheetBehaviorGoogleMapsLike.getPeekHeight] Y position to know if need to show the AppBar at beginning.
         */
        getBottomSheetBehavior(parent!!)
        if (mBottomSheetBehaviorRef == null || mBottomSheetBehaviorRef!!.get() == null)
            getBottomSheetBehavior(parent)
        val mCollapsedY = dependency.height - mBottomSheetBehaviorRef!!.get()!!.peekHeight
        mVisible = dependency.y >= mCollapsedY

        setStatusBarBackgroundVisible(mVisible)
        if (!mVisible) child.y = (child.y.toInt() - child.height - statusBarHeight).toFloat()
        mInit = true
        /**
         * Following [.onDependentViewChanged] docs, we need to return true if the
         * Behavior changed the child view's size or position, false otherwise.
         * In our case we only move it if mVisible got false in this method.
         */
        return !mVisible
    }

    fun setAppBarVisible(appBarLayout: AppBarLayout, visible: Boolean) {

        if (visible == mVisible)
            return

        if (mAppBarYValueAnimator == null || !mAppBarYValueAnimator!!.isRunning) {

            mAppBarYValueAnimator = ValueAnimator.ofFloat(
                appBarLayout.y,
                if (visible)
                    appBarLayout.y + appBarLayout.height + statusBarHeight
                else
                    appBarLayout.y - appBarLayout.height - statusBarHeight
            )
            mAppBarYValueAnimator!!.duration =
                (mContext.getResources().getInteger(android.R.integer.config_shortAnimTime)).toLong()
            mAppBarYValueAnimator!!.addUpdateListener { animation -> appBarLayout.y = animation.animatedValue as Float }
            mAppBarYValueAnimator!!.addListener(object : AnimatorListenerAdapter() {

                override  fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    if (visible)
                        setStatusBarBackgroundVisible(true)
                }

                override  fun onAnimationEnd(animation: Animator) {
                    if (!visible)
                        setStatusBarBackgroundVisible(false)
                    mVisible = visible
                    super.onAnimationEnd(animation)
                }
            })
            mAppBarYValueAnimator!!.start()
        }
    }

    private fun setStatusBarBackgroundVisible(visible: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (visible) {
                val window = (mContext as Activity).window
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = ContextCompat.getColor(mContext, R.color.colorPrimaryDark)
            } else {
                val window = (mContext as Activity).window
                window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = ContextCompat.getColor(mContext, android.R.color.transparent)
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
                    val temp = BottomSheetBehaviorGoogleMapsLike.from(child)
                    mBottomSheetBehaviorRef = WeakReference(temp)
                    break
                } catch (e: IllegalArgumentException) {
                }

            }
        }
    }

    protected class SavedState : View.BaseSavedState {

        internal val mVisible: Boolean

        constructor(source: Parcel) : super(source) {
            mVisible = source.readByte().toInt() != 0
        }

        constructor(superState: Parcelable, visible: Boolean) : super(superState) {
            this.mVisible = visible
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeByte((if (mVisible) 1 else 0).toByte())
        }

        companion object {

            @SuppressLint("ParcelCreator")
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState {
                    return SavedState(source)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    companion object {

        private val TAG = ScrollingAppBarLayoutBehavior::class.java.simpleName
    }
}