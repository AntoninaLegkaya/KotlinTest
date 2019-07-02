package com.fb.roottest.base.paralax

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.view.ViewPropertyAnimator
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.fb.roottest.R
import com.fb.roottest.custom.MergedAppBarLayout
import com.google.android.material.appbar.AppBarLayout
import java.lang.ref.WeakReference


internal class MergedAppBarBehavior(private val mContext: Context, attrs: AttributeSet) :
    AppBarLayout.ScrollingViewBehavior(mContext, attrs) {

    private var mInit = false

    private lateinit  var mBackGroundLayoutParams: FrameLayout.LayoutParams
    /**
     * To avoid using multiple "peekheight=" in XML and looking flexibility allowing [BottomSheetBehaviorGoogleMapsLike.mPeekHeight]
     * get changed dynamically we get the [NestedScrollView] that has
     * "app:layout_behavior=" [BottomSheetBehaviorGoogleMapsLike] inside the [CoordinatorLayout]
     */
    private var mBottomSheetBehaviorRef: WeakReference<BottomSheetBehaviorGoogleMapsLike<*>>? = null
    private var mInitialY: Float = 0.toFloat()
    private var mVisible = false

    private var mToolbarTitle: String = ""

    private var mToolbar: Toolbar? = null
    private var mTitleTextView: TextView? = null
    private lateinit var   mBackground: View
    private var mOnNavigationClickListener: View.OnClickListener? = null

    private var mTitleAlphaValueAnimator: ValueAnimator? = null
    private var mCurrentTitleAlpha = 0f

    private var isTitleVisible: Boolean
        get() = mTitleTextView!!.alpha == 1f
        set(visible) {

            if (visible && mTitleTextView!!.alpha == 1f || !visible && mTitleTextView!!.alpha == 0f)
                return

            if (mTitleAlphaValueAnimator == null || !mTitleAlphaValueAnimator!!.isRunning) {
                mToolbar!!.setTitle(mToolbarTitle)
                val startAlpha = if (visible) 0f else 1f
                mCurrentTitleAlpha = if (visible) 1f else 0f
                val endAlpha = mCurrentTitleAlpha

                mTitleAlphaValueAnimator = ValueAnimator.ofFloat(startAlpha, endAlpha)
                mTitleAlphaValueAnimator!!.duration = (mContext.getResources().getInteger(android.R.integer.config_shortAnimTime)).toLong()
                mTitleAlphaValueAnimator!!.addUpdateListener { animation ->
                    mTitleTextView!!.alpha = animation.animatedValue as Float
                }
                mTitleAlphaValueAnimator!!.start()
            }
        }

    private val isStatusBarVisible: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ((mContext as Activity).window.statusBarColor == ContextCompat.getColor(mContext, R.color.colorPrimaryDark))
        } else true

  override  fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        if (dependency is NestedScrollView) {
            try {
                BottomSheetBehaviorGoogleMapsLike.from<View>(dependency)
                return true
            } catch (e: IllegalArgumentException) {
            }

        }
        return false
    }

    override  fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {

        if (!mInit) {
            init(parent, child)
        }
        /**
         * Following docs we should return true if the Behavior changed the child view's size or position, false otherwise
         */
        var childMoved = false

        if (isDependencyYBelowAnchorPoint(parent, dependency)) {

            childMoved = setToolbarVisible(false, child)

        } else if (isDependencyYBetweenAnchorPointAndToolbar(parent, child, dependency)) {

            childMoved = setToolbarVisible(true, child)
            setFullBackGroundColor(android.R.color.transparent)
            setPartialBackGroundHeight(0)

        } else if (isDependencyYBelowToolbar(child, dependency) && !isDependencyYReachTop(dependency)) {

            childMoved = setToolbarVisible(true, child)
            if (isStatusBarVisible)
                setStatusBarBackgroundVisible(false)
            if (isTitleVisible)
                isTitleVisible = false
            setFullBackGroundColor(android.R.color.transparent)
            setPartialBackGroundHeight((child.getHeight() + child.y - dependency.y).toInt())

        } else if (isDependencyYBelowStatusToolbar(child, dependency) || isDependencyYReachTop(dependency)) {

            childMoved = setToolbarVisible(true, child)
            if (!isStatusBarVisible)
                setStatusBarBackgroundVisible(true)
            if (!isTitleVisible)
                isTitleVisible = true
            setFullBackGroundColor(R.color.colorPrimary)
            setPartialBackGroundHeight(0)
        }
        return childMoved
    }

    private fun init(parent: CoordinatorLayout, child: View) {

        if (child !is MergedAppBarLayout) throw IllegalArgumentException("The view is not a MergedAppBarLayout")

        val appBarLayout = child
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            appBarLayout.outlineProvider = ViewOutlineProvider.BACKGROUND
        }

        mToolbar = appBarLayout.toolbar
        mBackground = appBarLayout.background
        mBackGroundLayoutParams = (mBackground.getLayoutParams())as FrameLayout.LayoutParams
        getBottomSheetBehavior(parent)

        //TODO: modify the way we get this TextView with the MergedAppBarLayout.java
        mTitleTextView = findTitleTextView(mToolbar!!)
        if (mTitleTextView == null)
            return

        mInitialY = child.getY()

        child.setVisibility(if (mVisible) View.VISIBLE else View.INVISIBLE)
        //        setStatusBarBackgroundVisible(mVisible);

        setFullBackGroundColor(if (mVisible && mCurrentTitleAlpha == 1f) R.color.colorPrimary else android.R.color.transparent)
        setPartialBackGroundHeight(0)
        mTitleTextView!!.text = mToolbarTitle
        mTitleTextView!!.alpha = mCurrentTitleAlpha.toFloat()
        mInit = true
        setToolbarVisible(false, child)
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

    private fun isDependencyYBelowAnchorPoint(parent: CoordinatorLayout, dependency: View): Boolean {
        if (mBottomSheetBehaviorRef == null || mBottomSheetBehaviorRef!!.get() == null)
            getBottomSheetBehavior(parent)
        return dependency.getY() > mBottomSheetBehaviorRef?.get()!!.anchorPoint
    }

    private fun isDependencyYBetweenAnchorPointAndToolbar(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        if (mBottomSheetBehaviorRef == null || mBottomSheetBehaviorRef!!.get() == null)
            getBottomSheetBehavior(parent)
        return dependency.getY() <= mBottomSheetBehaviorRef?.get()?.anchorPoint!! && dependency.getY() > child.getY() + child.getHeight()
    }

    private fun isDependencyYBelowToolbar(child: View, dependency: View): Boolean {
        return dependency.getY() <= child.getY() + child.getHeight() && dependency.getY() > child.getY()
    }

    private fun isDependencyYBelowStatusToolbar(child: View, dependency: View): Boolean {
        return dependency.getY() <= child.getY()
    }

    private fun isDependencyYReachTop(dependency: View): Boolean {
        return dependency.y.toInt() === 0
    }

    private fun setPartialBackGroundHeight(height: Int) {
        mBackGroundLayoutParams.height=height
        mBackground.setLayoutParams(mBackGroundLayoutParams)
    }

    private fun setFullBackGroundColor(@ColorRes colorRes: Int) {
        mToolbar!!.setBackgroundColor(ContextCompat.getColor(mContext, colorRes))
    }

    private fun findTitleTextView(toolbar: Toolbar): TextView? {
        for (i in 0 until toolbar.getChildCount()) {
            val toolBarChild = toolbar.getChildAt(i)
            if (toolBarChild is TextView &&
                (toolBarChild ).text != null &&
                (toolBarChild ).text.toString().contentEquals(mContext.getResources().getString(R.string.key_binding_default_toolbar_name))
            ) {
                return toolBarChild
            }
        }
        return null
    }

    private fun setToolbarVisible(visible: Boolean, child: View): Boolean {
        val mAppBarLayoutAnimation: ViewPropertyAnimator
        var childMoved = false
        if (visible && !mVisible) {
            childMoved = true
            child.setY((-child.getHeight() / 3).toFloat())
            mAppBarLayoutAnimation =
                child.animate().setDuration(mContext.getResources().getInteger(android.R.integer.config_shortAnimTime).toLong())
            mAppBarLayoutAnimation.setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    child.setVisibility(View.VISIBLE)
                }

                override  fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    (mContext as AppCompatActivity).setSupportActionBar(mToolbar)
                    mToolbar!!.setNavigationOnClickListener(mOnNavigationClickListener)
                    val actionBar = (mContext as AppCompatActivity).supportActionBar
                    if (actionBar != null) {
                        actionBar.setDisplayHomeAsUpEnabled(true)
                    }
                    mVisible = true
                }
            })
            mAppBarLayoutAnimation.alpha(1f).y(mInitialY).start()
        } else if (!visible && mVisible) {
            mAppBarLayoutAnimation =
                child.animate().setDuration((mContext.getResources().getInteger(android.R.integer.config_shortAnimTime)).toLong())
            mAppBarLayoutAnimation.setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    child.setVisibility(View.INVISIBLE)
                }

                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    (mContext as AppCompatActivity).setSupportActionBar(null)
                    mVisible = false
                }
            })
            mAppBarLayoutAnimation.alpha(0f).start()
        }

        return childMoved
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

    fun setNavigationOnClickListener(listener: View.OnClickListener) {
        this.mOnNavigationClickListener = listener
    }

    fun setToolbarTitle(title: String) {
        this.mToolbarTitle = title
        if (this.mToolbar != null)
            this.mToolbar!!.setTitle(title)
    }

    override fun onSaveInstanceState(parent: CoordinatorLayout, child: View): Parcelable {
       return SavedState(
           super.onSaveInstanceState(parent, child)!!,
            mVisible,
            mToolbarTitle,
            mCurrentTitleAlpha
            )
    }

    override  fun onRestoreInstanceState(parent: CoordinatorLayout, child: View, state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(parent, child, ss.getSuperState())
        this.mVisible = ss.mVisible
        this.mToolbarTitle = ss.mToolbarTitle.toString()
        this.mCurrentTitleAlpha = ss.mTitleAlpha
    }

    protected class SavedState : View.BaseSavedState {

        internal val mVisible: Boolean
        internal val mToolbarTitle: String?
        internal val mTitleAlpha: Float

        constructor(source: Parcel) : super(source) {
            mVisible = source.readByte().toInt() != 0
            mToolbarTitle = source.readString()
            mTitleAlpha = source.readFloat()
        }

        constructor(
            superState: Parcelable,
            visible: Boolean,
            toolBarTitle: String,
            titleAlpha: Float
        ) : super(superState) {
            this.mVisible = visible
            this.mToolbarTitle = toolBarTitle
            this.mTitleAlpha = titleAlpha
        }

        override     fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeByte((if (mVisible) 1 else 0).toByte())
            out.writeString(mToolbarTitle)
            out.writeFloat(mTitleAlpha)
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

        private val TAG = MergedAppBarBehavior::class.java.simpleName
        @JvmStatic
        fun <V : View> from(view: V): MergedAppBarBehavior {
            val params = view.getLayoutParams()
            if (params !is CoordinatorLayout.LayoutParams) {
                throw IllegalArgumentException("The view is not a child of CoordinatorLayout")
            }
            val behavior = (params as CoordinatorLayout.LayoutParams)
                .behavior
            if (behavior !is MergedAppBarBehavior) {
                throw IllegalArgumentException("The view is not associated with " + "MergedAppBarLayoutBehavior")
            }
            return behavior
        }
    }
}