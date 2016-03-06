package com.ybao.pullrefreshview.layout;

import android.content.Context;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.Scroller;

import com.ybao.pullrefreshview.utils.CanPullUtil;
import com.ybao.pullrefreshview.utils.Pullable;

/**
 * Created by ybao on 16/3/6.
 */
public class FlingLayout extends FrameLayout implements NestedScrollingParent, NestedScrollingChild {
    private NestedScrollingParentHelper mParentHelper;
    private NestedScrollingChildHelper mChildHelper;
    protected OnScrollListener mOnScrollListener;
    private int mTouchSlop;
    private Scroller mScroller;
    public final static int NONE = 0;
    public final static int SCROLLING = 1;
    public final static int FLING = 2;
    private int stateType = NONE;
    private static final int MAX_DURATION = 300;
    protected Pullable pullable;
    protected View mPullView;
    protected int maxHeaderDistance = 0;
    protected int maxFooterDistance = 0;

    public FlingLayout(Context context) {
        this(context, null);
    }

    public FlingLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlingLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mParentHelper = new NestedScrollingParentHelper(this);
        mChildHelper = new NestedScrollingChildHelper(this);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mScroller = new Scroller(context, new DecelerateInterpolator());
        setNestedScrollingEnabled(true);
    }

    @Override
    public void computeScroll() {
        if (!mScroller.isFinished()) {
            if (mScroller.computeScrollOffset()) {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
                postInvalidate();
            }
        }
        super.computeScroll();
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        mParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
    }

    @Override
    public void onStopNestedScroll(View target) {
        fling(getOffsetTop());
        stopNestedScroll();
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        int[] offsetInWindow = new int[2];
        dispatchNestedScroll(0, dyConsumed, 0, dyUnconsumed, offsetInWindow);
        scrollBy(0, dyUnconsumed + offsetInWindow[1]);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        dispatchNestedPreScroll(0, dy, consumed, null);
        dy -= consumed[1];
        int offsetTop = getOffsetTop();
        if (offsetTop < 0) {
            if (offsetTop + dy > 0) {
                scrollTo(0, 0);
                dy = 0 - offsetTop;
            } else {
                if (dy > 0) {
                    scrollBy(0, dy);
                } else {
                    if (maxHeaderDistance == 0 || offsetTop + dy > -maxHeaderDistance) {
                        scrollBy(0, dy / 2);
                    } else {
                        scrollTo(0, -maxHeaderDistance);
                    }
                }
            }
            consumed[0] = 0;
            consumed[1] += dy;
        } else if (offsetTop > 0) {
            if (offsetTop + dy < 0) {
                scrollTo(0, 0);
                dy = 0 - offsetTop;
            } else {
                if (dy < 0) {
                    scrollBy(0, dy);
                } else {
                    if (maxFooterDistance == 0 || offsetTop + dy < maxFooterDistance) {
                        scrollBy(0, dy / 2);
                    } else {
                        scrollTo(0, maxFooterDistance);
                    }
                }
            }
            consumed[0] = 0;
            consumed[1] += dy;
        }
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        boolean consumed = dispatchNestedPreFling(velocityX, velocityY);
        if (consumed) {
            return true;
        }
        Pullable pullable = CanPullUtil.getPullAble(target);
        if (pullable != null) {
            if (pullable.isGetBottom() && velocityY < 0) {
                return true;
            } else if (pullable.isGetTop() && velocityY > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getNestedScrollAxes() {
        return mParentHelper.getNestedScrollAxes();
    }


    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mChildHelper.stopNestedScroll();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mChildHelper.onDetachedFromWindow();
    }

    protected void fling(int offsetTop) {
        startScrollTo(offsetTop, 0);
    }

    public int startScrollBy(int startY, int dy) {
        setState(FLING, startY + dy);
        int duration = Math.abs(dy);
        int time = duration > MAX_DURATION ? MAX_DURATION : duration;
        mScroller.startScroll(0, startY, 0, dy, time);
        invalidate();
        return time;
    }

    public int startScrollTo(int startY, int endY) {
        return startScrollBy(startY, endY - startY);
    }


    protected void onScroll(int y) {

    }

    protected void onScrollChange(int state, int y) {

    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (mPullView == null && (pullable = CanPullUtil.getPullAble(child)) != null) {
            mPullView = child;
        }
        super.addView(child, index, params);
    }

    public int getOffsetTop() {
        return getScrollY();
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        onScroll(y);
        if (mOnScrollListener != null) {
            mOnScrollListener.onScroll(this, y);
        }
    }

    @Override
    public void scrollBy(int x, int y) {
        scrollTo(getScrollX() + x, getOffsetTop() + y);
    }


    private void setState(int state, int y) {
        if (stateType != state || y != getOffsetTop()) {
            onScrollChange(state, y);
            if (mOnScrollListener != null) {
                mOnScrollListener.onScrollChange(this, state, y);
            }
        }
        stateType = state;
    }


    public interface OnScrollListener {
        void onScroll(FlingLayout flingLayout, int y);

        void onScrollChange(FlingLayout flingLayout, int state, int y);
    }

    public void setOnScrollListener(OnScrollListener mOnScrollListener) {
        this.mOnScrollListener = mOnScrollListener;
    }

    /**
     * 用来判断view在竖直方向上能不能向上或者向下滑动
     *
     * @param view      v
     * @param direction 方向    负数代表向上滑动 ，正数则反之
     * @return
     */
    public boolean canScrollVertically(View view, int direction) {
        return ViewCompat.canScrollVertically(view, direction);
    }

    public void setMaxHeaderDistance(int maxHeaderDistance) {
        this.maxHeaderDistance = maxHeaderDistance;
    }

    public void setMaxFooterDistance(int maxFooterDistance) {
        this.maxFooterDistance = maxFooterDistance;
    }
}
