package com.fb.roottest.base.paralax

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.NestedScrollingChild
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import com.fb.roottest.R
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.ref.WeakReference
import java.util.*


class BottomSheetBehaviorGoogleMapsLike<V : View> : CoordinatorLayout.Behavior<V> {

    private val mMinimumVelocity: Float

    /**
     * Gets the height of the bottom sheet when it is collapsed.
     *
     * @return The height of the collapsed bottom sheet.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Params_behavior_peekHeight
     */
    /**
     * Sets the height of the bottom sheet when it is collapsed.
     *
     * @param peekHeight The height of the collapsed bottom sheet in pixels.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Params_behavior_peekHeight
     */
    var peekHeight: Int = 0
        set(peekHeight) {
            field = Math.max(0, peekHeight)
            mMaxOffset = mParentHeight - peekHeight
        }

    private var mMinOffset: Int = 0
    private var mMaxOffset: Int = 0
    var anchorPoint: Int = 0

    /**
     * Gets whether this bottom sheet can hide when it is swiped down.
     *
     * @return `true` if this bottom sheet can hide.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Params_behavior_hideable
     */
    /**
     * Sets whether this bottom sheet can hide when it is swiped down.
     *
     * @param hideable `true` to make this bottom sheet hideable.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Params_behavior_hideable
     */
    var isHideable: Boolean = false

    /**
     * Gets whether some states should be skipped.
     *
     * @return `true` if this bottom sheet can hide.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Params_behavior_hideable
     */
    /**
     * Sets whether some states should be skipped.
     *
     * @param collapsible `true` to make this bottom sheet hideable.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Params_behavior_hideable
     */
    var isCollapsible: Boolean = false

    @State
    private var mState = STATE_ANCHOR_POINT
    @State
    private var mLastStableState =
        STATE_ANCHOR_POINT

    private var mViewDragHelper: ViewDragHelper? = null

    private var mIgnoreEvents: Boolean = false

    private var mNestedScrolled: Boolean = false

    private var mParentHeight: Int = 0

    private var mViewRef: WeakReference<V>? = null

    private var mNestedScrollingChildRef: WeakReference<View>? = null

    private var mCallback: Vector<BottomSheetCallback>? = null

    private var mActivePointerId: Int = 0

    private var mInitialY: Int = 0

    private var mTouchingScrollingChild: Boolean = false

    private val mScrollVelocityTracker = ScrollVelocityTracker()

    /**
     * Gets the current state of the bottom sheet.
     *
     * @return One of [.STATE_EXPANDED], [.STATE_ANCHOR_POINT], [.STATE_COLLAPSED],
     * [.STATE_DRAGGING], and [.STATE_SETTLING].
     */
    /**
     * Sets the state of the bottom sheet. The bottom sheet will transition to that state with
     * animation.
     *
     * @param state One of [.STATE_COLLAPSED], [.STATE_ANCHOR_POINT],
     * [.STATE_EXPANDED] or [.STATE_HIDDEN].
     */
    /**
     * New behavior (added: state == STATE_ANCHOR_POINT ||)
     */
    var state: Int
        @State
        get() = mState
        set(@State state) {
            if (state == mState) {
                return
            }
            if (state == STATE_COLLAPSED || state == STATE_EXPANDED || state == STATE_ANCHOR_POINT ||
                isHideable && state == STATE_HIDDEN
            ) {
                mState = state
                mLastStableState = state
            }

            val child = (if (mViewRef == null) null else mViewRef!!.get()) ?: return
            val top: Int
            if (state == STATE_COLLAPSED) {
                top = mMaxOffset
            } else if (state == STATE_ANCHOR_POINT) {
                top = anchorPoint
            } else if (state == STATE_EXPANDED) {
                top = mMinOffset
            } else if (isHideable && state == STATE_HIDDEN) {
                top = mParentHeight
            } else {
                throw IllegalArgumentException("Illegal state argument: $state")
            }
            setStateInternal(STATE_SETTLING)
            if (mViewDragHelper!!.smoothSlideViewTo(child, child.left, top)) {
                ViewCompat.postOnAnimation(child, SettleRunnable(child, state))
            }
        }

    private val mDragCallback = object : ViewDragHelper.Callback() {

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            if (mState == STATE_DRAGGING) {
                return false
            }
            if (mTouchingScrollingChild) {
                return false
            }
            if (mState == STATE_EXPANDED && mActivePointerId == pointerId) {
                val scroll = mNestedScrollingChildRef!!.get()
                if (scroll != null && scroll.canScrollVertically(-1)) {
                    // Let the content scroll up
                    return false
                }
            }
            return mViewRef != null && mViewRef!!.get() === child
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            dispatchOnSlide(top)
        }

        override fun onViewDragStateChanged(state: Int) {
            if (state == ViewDragHelper.STATE_DRAGGING) {
                setStateInternal(STATE_DRAGGING)
            }
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            var top: Int
            @State var targetState: Int
            if (yvel < 0) { // Moving up
                top = mMinOffset
                targetState = STATE_EXPANDED
            } else if (isHideable && shouldHide(releasedChild, yvel)) {
                top = mParentHeight
                targetState = STATE_HIDDEN
            } else if (yvel == 0f) {
                val currentTop = releasedChild.top
                if (Math.abs(currentTop - mMinOffset) < Math.abs(currentTop - mMaxOffset)) {
                    top = mMinOffset
                    targetState =
                        STATE_EXPANDED
                } else {
                    top = mMaxOffset
                    targetState =
                        STATE_COLLAPSED
                }
            } else {
                top = mMaxOffset
                targetState = STATE_COLLAPSED
            }

            // Restrict Collapsed view (optional)
            if (!isCollapsible && targetState == STATE_COLLAPSED) {
                top = anchorPoint
                targetState =
                    STATE_ANCHOR_POINT
            }

            if (mViewDragHelper!!.settleCapturedViewAt(releasedChild.left, top)) {
                setStateInternal(STATE_SETTLING)
                ViewCompat.postOnAnimation(
                    releasedChild,
                    SettleRunnable(releasedChild, targetState)
                )
            } else {
                setStateInternal(targetState)
            }
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return constrain(top, mMinOffset, if (isHideable) mParentHeight else mMaxOffset)
        }

        internal fun constrain(amount: Int, low: Int, high: Int): Int {
            return if (amount < low) low else if (amount > high) high else amount
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return child.left
        }

        override fun getViewVerticalDragRange(child: View): Int {
            return if (isHideable) {
                mParentHeight - mMinOffset
            } else {
                mMaxOffset - mMinOffset
            }
        }
    }

    /**
     * Callback for monitoring events about bottom sheets.
     */
    abstract class BottomSheetCallback {

        /**
         * Called when the bottom sheet changes its state.
         *
         * @param bottomSheet The bottom sheet view.
         * @param newState    The new state. This will be one of [.STATE_DRAGGING],
         * [.STATE_SETTLING], [.STATE_ANCHOR_POINT],
         * [.STATE_EXPANDED],
         * [.STATE_COLLAPSED], or [.STATE_HIDDEN].
         */
        abstract fun onStateChanged(bottomSheet: View, @State newState: Int)

        /**
         * Called when the bottom sheet is being dragged.
         *
         * @param bottomSheet The bottom sheet view.
         * @param slideOffset The new offset of this bottom sheet within its range, from 0 to 1
         * when it is moving upward, and from 0 to -1 when it moving downward.
         */
        abstract fun onSlide(bottomSheet: View, slideOffset: Float)
    }

    /** @hide
     */
    @IntDef(
        STATE_EXPANDED,
        STATE_COLLAPSED,
        STATE_DRAGGING,
        STATE_ANCHOR_POINT,
        STATE_SETTLING,
        STATE_HIDDEN
    )
    @Retention(RetentionPolicy.SOURCE)
    annotation class State

    /**
     * Default constructor for instantiating BottomSheetBehaviors.
     */
    constructor() {}

    /**
     * Default constructor for inflating BottomSheetBehaviors from layout.
     *
     * @param context The [Context].
     * @param attrs   The [AttributeSet].
     */
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        var a = context.obtainStyledAttributes(
            attrs,
            com.google.android.material.R.styleable.BottomSheetBehavior_Layout
        )
        peekHeight = a.getDimensionPixelSize(
            com.google.android.material.R.styleable.BottomSheetBehavior_Layout_behavior_peekHeight, 0
        )
        isHideable =
            a.getBoolean(com.google.android.material.R.styleable.BottomSheetBehavior_Layout_behavior_hideable, false)
        a.recycle()

        /**
         * Getting the anchorPoint...
         */
        anchorPoint = DEFAULT_ANCHOR_POINT
        isCollapsible = true
        a = context.obtainStyledAttributes(attrs, R.styleable.CustomBottomSheetBehavior)
        if (attrs != null) {
            anchorPoint = a.getDimension(R.styleable.CustomBottomSheetBehavior_anchorPoint, 0f).toInt()
            mState = a.getInt(R.styleable.CustomBottomSheetBehavior_defaultStateBehavior,
                STATE_ANCHOR_POINT
            )
        }
        a.recycle()

        val configuration = ViewConfiguration.get(context)
        mMinimumVelocity = configuration.scaledMinimumFlingVelocity.toFloat()
    }

    override fun onSaveInstanceState(parent: CoordinatorLayout, child: V): Parcelable? {
        return super.onSaveInstanceState(parent, child)?.let {
            SavedState(
                it,
                mState
            )
        }
    }

    override fun onRestoreInstanceState(parent: CoordinatorLayout, child: V, state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(parent, child, ss.superState)
        // Intermediate states are restored as collapsed state
        if (ss.state == STATE_DRAGGING || ss.state == STATE_SETTLING) {
            mState = STATE_COLLAPSED
        } else {
            mState = ss.state
        }

        mLastStableState = mState
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
        // First let the parent lay it out
        if (mState != STATE_DRAGGING && mState != STATE_SETTLING) {
            if (parent.fitsSystemWindows && !child.fitsSystemWindows) {
                child.fitsSystemWindows = true
            }
            parent.onLayoutChild(child, layoutDirection)
        }
        // Offset the bottom sheet
        mParentHeight = parent.height
        mMinOffset = Math.max(0, mParentHeight - child.height)
        mMaxOffset = Math.max(mParentHeight - peekHeight, mMinOffset)

        /**
         * New behavior
         */
        if (mState == STATE_ANCHOR_POINT) {
            ViewCompat.offsetTopAndBottom(child, anchorPoint)
        } else if (mState == STATE_EXPANDED) {
            ViewCompat.offsetTopAndBottom(child, mMinOffset)
        } else if (isHideable && mState == STATE_HIDDEN) {
            ViewCompat.offsetTopAndBottom(child, mParentHeight)
        } else if (mState == STATE_COLLAPSED) {
            ViewCompat.offsetTopAndBottom(child, mMaxOffset)
        }
        if (mViewDragHelper == null) {
            mViewDragHelper = ViewDragHelper.create(parent, mDragCallback)
        }
        mViewRef = WeakReference(child)
        mNestedScrollingChildRef = WeakReference<View>(findScrollingChild(child))
        return true
    }

    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        if (!child.isShown) {
            mIgnoreEvents = true
            return false
        }

        val action = event.actionMasked
        if (action == MotionEvent.ACTION_DOWN) {
            reset()
        }

        when (action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mTouchingScrollingChild = false
                mActivePointerId = MotionEvent.INVALID_POINTER_ID
                // Reset the ignore flag
                if (mIgnoreEvents) {
                    mIgnoreEvents = false
                    return false
                }
            }
            MotionEvent.ACTION_DOWN -> {
                mScrollVelocityTracker.clear()
                val initialX = event.x.toInt()
                mInitialY = event.y.toInt()
                if (mState == STATE_ANCHOR_POINT) {
                    mActivePointerId = event.getPointerId(event.actionIndex)
                    mTouchingScrollingChild = true
                } else {
                    val scroll = mNestedScrollingChildRef!!.get()
                    if (scroll != null && parent.isPointInChildBounds(scroll, initialX, mInitialY)) {
                        mActivePointerId = event.getPointerId(event.actionIndex)
                        mTouchingScrollingChild = true
                    }
                }
                mIgnoreEvents =
                    mActivePointerId == MotionEvent.INVALID_POINTER_ID && !parent.isPointInChildBounds(
                        child,
                        initialX,
                        mInitialY
                    )
            }
            MotionEvent.ACTION_MOVE -> {
            }
        }

        if (!mIgnoreEvents && mViewDragHelper!!.shouldInterceptTouchEvent(event)) {
            return true
        }
        // We have to handle cases that the ViewDragHelper does not capture the bottom sheet because
        // it is not the top most view of its parent. This is not necessary when the touch event is
        // happening over the scrolling content as nested scrolling logic handles that case.
        val scroll = mNestedScrollingChildRef!!.get()
        return action == MotionEvent.ACTION_MOVE && scroll != null &&
                !mIgnoreEvents && mState != STATE_DRAGGING &&
                !parent.isPointInChildBounds(scroll, event.x.toInt(), event.y.toInt()) &&
                Math.abs(mInitialY - event.y) > mViewDragHelper!!.touchSlop
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        if (!child.isShown) {
            return false
        }

        val action = event.actionMasked
        if (mState == STATE_DRAGGING && action == MotionEvent.ACTION_DOWN) {
            return true
        }

        // Detect scroll direction for ignoring collapsible
        if (mLastStableState == STATE_ANCHOR_POINT && action == MotionEvent.ACTION_MOVE) {
            if (event.y > mInitialY && !isCollapsible) {
                reset()
                return false
            }
        }

        if (mViewDragHelper == null) {
            mViewDragHelper = ViewDragHelper.create(parent, mDragCallback)
        }

        mViewDragHelper!!.processTouchEvent(event)

        if (action == MotionEvent.ACTION_DOWN) {
            reset()
        }

        // The ViewDragHelper tries to capture only the top-most View. We have to explicitly tell it
        // to capture the bottom sheet in case it is not captured and the touch slop is passed.
        if (action == MotionEvent.ACTION_MOVE && !mIgnoreEvents) {
            if (Math.abs(mInitialY - event.y) > mViewDragHelper!!.touchSlop) {
                mViewDragHelper!!.captureChildView(child, event.getPointerId(event.actionIndex))
            }
        }
        return !mIgnoreEvents
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout, child: V,
        directTargetChild: View, target: View, nestedScrollAxes: Int,
        @ViewCompat.NestedScrollType type: Int
    ): Boolean {
        mNestedScrolled = false
        return nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
    }

    private inner class ScrollVelocityTracker {
        private var mPreviousScrollTime: Long = 0
        var scrollVelocity = 0f
            private set

        fun recordScroll(dy: Int) {
            val now = System.currentTimeMillis()

            if (mPreviousScrollTime != 0L) {
                val elapsed = now - mPreviousScrollTime
                scrollVelocity = dy.toFloat() / elapsed * 1000 // pixels per sec
            }

            mPreviousScrollTime = now
        }

        fun clear() {
            mPreviousScrollTime = 0
            scrollVelocity = 0f
        }
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout, child: V, target: View,
        dx: Int, dy: Int, consumed: IntArray,
        @ViewCompat.NestedScrollType type: Int
    ) {
        val scrollingChild = mNestedScrollingChildRef!!.get()
        if (target !== scrollingChild) {
            return
        }

        mScrollVelocityTracker.recordScroll(dy)

        val currentTop = child.top
        val newTop = currentTop - dy

        // Force stop at the anchor - do not go from collapsed to expanded in one scroll
        if (mLastStableState == STATE_COLLAPSED && newTop < anchorPoint || mLastStableState == STATE_EXPANDED && newTop > anchorPoint) {
            consumed[1] = dy
            ViewCompat.offsetTopAndBottom(child, anchorPoint - currentTop)
            dispatchOnSlide(child.top)
            mNestedScrolled = true
            return
        }

        if (dy > 0) { // Upward
            if (newTop < mMinOffset) {
                consumed[1] = currentTop - mMinOffset
                ViewCompat.offsetTopAndBottom(child, -consumed[1])
                setStateInternal(STATE_EXPANDED)
            } else {
                consumed[1] = dy
                ViewCompat.offsetTopAndBottom(child, -dy)
                setStateInternal(STATE_DRAGGING)
            }
        } else if (dy < 0) { // Downward
            if (!ViewCompat.canScrollVertically(target, -1)) {
                if (newTop <= mMaxOffset || isHideable) {
                    // Restrict STATE_COLLAPSED if restrictedState is set
                    if (isCollapsible == true || isCollapsible == false && anchorPoint - newTop >= 0) {
                        consumed[1] = dy
                        ViewCompat.offsetTopAndBottom(child, -dy)
                        setStateInternal(STATE_DRAGGING)
                    }
                } else {
                    consumed[1] = currentTop - mMaxOffset
                    ViewCompat.offsetTopAndBottom(child, -consumed[1])
                    setStateInternal(STATE_COLLAPSED)
                }
            }
        }
        dispatchOnSlide(child.top)
        mNestedScrolled = true
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout, child: V, target: View,
        @ViewCompat.NestedScrollType type: Int
    ) {
        if (child.top == mMinOffset) {
            setStateInternal(STATE_EXPANDED)
            mLastStableState = STATE_EXPANDED
            return
        }
        if (target !== mNestedScrollingChildRef!!.get() || !mNestedScrolled) {
            return
        }
        val top: Int
        val targetState: Int

        // Are we flinging up?
        val scrollVelocity = mScrollVelocityTracker.scrollVelocity
        if (scrollVelocity > mMinimumVelocity) {
            if (mLastStableState == STATE_COLLAPSED) {
                // Fling from collapsed to anchor
                top = anchorPoint
                targetState =
                    STATE_ANCHOR_POINT
            } else if (mLastStableState == STATE_ANCHOR_POINT) {
                // Fling from anchor to expanded
                top = mMinOffset
                targetState = STATE_EXPANDED
            } else {
                // We are already expanded
                top = mMinOffset
                targetState = STATE_EXPANDED
            }
        } else
        // Are we flinging down?
            if (scrollVelocity < -mMinimumVelocity) {
                if (mLastStableState == STATE_EXPANDED) {
                    // Fling to from expanded to anchor
                    top = anchorPoint
                    targetState =
                        STATE_ANCHOR_POINT
                } else if (isCollapsible == true) {
                    if (mLastStableState == STATE_ANCHOR_POINT) {
                        // Fling from anchor to collapsed
                        top = mMaxOffset
                        targetState =
                            STATE_COLLAPSED
                    } else {
                        // We are already collapsed
                        top = mMaxOffset
                        targetState =
                            STATE_COLLAPSED
                    }
                } else {
                    top = anchorPoint
                    targetState =
                        STATE_ANCHOR_POINT
                }
            } else {
                // Collapse?
                val currentTop = child.top
                if (currentTop > anchorPoint * 1.25 && isCollapsible == true) { // Multiply by 1.25 to account for parallax. The currentTop needs to be pulled down 50% of the anchor point before collapsing.
                    top = mMaxOffset
                    targetState =
                        STATE_COLLAPSED
                } else if (currentTop < anchorPoint * 0.5) {
                    top = mMinOffset
                    targetState =
                        STATE_EXPANDED
                } else {
                    top = anchorPoint
                    targetState =
                        STATE_ANCHOR_POINT
                }// Snap back to the anchor
                // Expand?
            }// Not flinging, just settle to the nearest state

        mLastStableState = targetState
        if (mViewDragHelper!!.smoothSlideViewTo(child, child.left, top)) {
            setStateInternal(STATE_SETTLING)
            ViewCompat.postOnAnimation(child, SettleRunnable(child, targetState))
        } else {
            setStateInternal(targetState)
        }
        mNestedScrolled = false
    }

    override fun onNestedPreFling(
        coordinatorLayout: CoordinatorLayout, child: V, target: View,
        velocityX: Float, velocityY: Float
    ): Boolean {
        return target === mNestedScrollingChildRef!!.get() && (mState != STATE_EXPANDED || super.onNestedPreFling(
            coordinatorLayout, child, target,
            velocityX, velocityY
        ))
    }

    /**
     * Adds a callback to be notified of bottom sheet events.
     *
     * @param callback The callback to notify when bottom sheet events occur.
     */
    fun addBottomSheetCallback(callback: BottomSheetCallback) {
        if (mCallback == null)
            mCallback = Vector()

        mCallback!!.add(callback)
    }

    private fun setStateInternal(@State state: Int) {
        if (mState == state) {
            return
        }
        mState = state
        val bottomSheet = mViewRef!!.get()
        if (bottomSheet != null && mCallback != null) {
            //            mCallback.onStateChanged(bottomSheet, state);
            notifyStateChangedToListeners(bottomSheet, state)
        }
    }

    private fun notifyStateChangedToListeners(bottomSheet: View, @State newState: Int) {
        for (bottomSheetCallback in mCallback!!) {
            bottomSheetCallback.onStateChanged(bottomSheet, newState)
        }
    }

    private fun notifyOnSlideToListeners(bottomSheet: View, slideOffset: Float) {
        for (bottomSheetCallback in mCallback!!) {
            bottomSheetCallback.onSlide(bottomSheet, slideOffset)
        }
    }

    private fun reset() {
        mActivePointerId = ViewDragHelper.INVALID_POINTER
    }

    private fun shouldHide(child: View, yvel: Float): Boolean {
        if (child.top < mMaxOffset) {
            // It should not hide, but collapse.
            return false
        }
        val newTop = child.top + yvel * HIDE_FRICTION
        return Math.abs(newTop - mMaxOffset) / peekHeight.toFloat() > HIDE_THRESHOLD
    }

    private fun findScrollingChild(view: View): View? {
        if (view is NestedScrollingChild) {
            return view
        }
        if (view is ViewGroup) {
            var i = 0
            val count = view.childCount
            while (i < count) {
                val scrollingChild = findScrollingChild(view.getChildAt(i))
                if (scrollingChild != null) {
                    return scrollingChild
                }
                i++
            }
        }
        return null
    }

    private fun dispatchOnSlide(top: Int) {
        val bottomSheet = mViewRef!!.get()
        if (bottomSheet != null && mCallback != null) {
            if (top > mMaxOffset) {
                notifyOnSlideToListeners(bottomSheet, (mMaxOffset - top).toFloat() / peekHeight)
            } else {
                notifyOnSlideToListeners(bottomSheet, (mMaxOffset - top).toFloat() / (mMaxOffset - mMinOffset))
            }
        }
    }

    private inner class SettleRunnable internal constructor(
        private val mView: View, @param:State @field:State
        private val mTargetState: Int
    ) : Runnable {

        override fun run() {
            if (mViewDragHelper != null && mViewDragHelper!!.continueSettling(true)) {
                ViewCompat.postOnAnimation(mView, this)
            } else {
                setStateInternal(mTargetState)
            }
        }
    }

    protected class SavedState : View.BaseSavedState {

        @State
        internal val state: Int

        constructor(source: Parcel) : super(source) {

            state = source.readInt()
        }

        constructor(superState: Parcelable, @State state: Int) : super(superState) {
            this.state = state
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(state)
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

        /**
         * The bottom sheet is dragging.
         */
        const val STATE_DRAGGING = 1

        /**
         * The bottom sheet is settling.
         */
        const val STATE_SETTLING = 2

        /**
         * The bottom sheet is expanded_half_way.
         */
        const val STATE_ANCHOR_POINT = 3

        /**
         * The bottom sheet is expanded.
         */
        const val STATE_EXPANDED = 4

        /**
         * The bottom sheet is collapsed.
         */
        const val STATE_COLLAPSED = 5

        /**
         * The bottom sheet is hidden.
         */
        const val STATE_HIDDEN = 6

        private val HIDE_THRESHOLD = 0.5f
        private val HIDE_FRICTION = 0.1f

        private val DEFAULT_ANCHOR_POINT = 700

        /**
         * A utility function to get the [BottomSheetBehaviorGoogleMapsLike] associated with the `view`.
         *
         * @param view The [View] with [BottomSheetBehaviorGoogleMapsLike].
         * @param <V> Instance of behavior
         * @return The [BottomSheetBehaviorGoogleMapsLike] associated with the `view`.
        </V> */
        @JvmStatic
        fun <V : View> from(view: V): BottomSheetBehaviorGoogleMapsLike<V> {
            val params = view.layoutParams as? CoordinatorLayout.LayoutParams
                ?: throw IllegalArgumentException("The view is not a child of CoordinatorLayout")
            val behavior = params
                .behavior as? BottomSheetBehaviorGoogleMapsLike<*> ?: throw IllegalArgumentException(
                "The view is not associated with BottomSheetBehaviorGoogleMapsLike"
            )
            return behavior as BottomSheetBehaviorGoogleMapsLike<V>
        }
    }

}