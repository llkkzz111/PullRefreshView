/**
 * Copyright 2015 Pengyuan-Jiang
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * Author：Ybao on 2015/11/5  ‏‎17:49
 * <p/>
 * QQ: 392579823
 * <p/>
 * Email：392579823@qq.com
 */
package com.ybao.pullrefreshview.layout;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.ybao.pullrefreshview.utils.CanPullUtil;
import com.ybao.pullrefreshview.utils.Pullable;

public class FlingLayout extends FrameLayout {

    public final static int NONE = 0;
    public final static int SCROLLING = 1;
    public final static int FLING = 2;
    private int stateType = NONE;

    protected Pullable pullable;
    protected View mPullView;
    private int mTouchSlop;
    private Scroller mScroller;
    protected float downY, downX;
    private boolean isScrolling = false;
    protected float tepmY;
    private static final int MAX_DURATION = 300;
    private boolean canPullUp = true;
    private boolean canPullDown = true;
    protected OnScrollListener mOnScrollListener;
    protected int maxDistance = 0;
    protected int version;
    int mPointerId;
    protected int MAXDISTANCE = 0;

    public FlingLayout(Context context) {
        this(context, null);
    }

    public FlingLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlingLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void init(Context context) {
        version = android.os.Build.VERSION.SDK_INT;
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mScroller = new Scroller(context, new DecelerateInterpolator());
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        MAXDISTANCE = getMeasuredHeight() * 3 / 5;
    }

    @Override
    public void computeScroll() {
        if (!mScroller.isFinished()) {
            if (mScroller.computeScrollOffset()) {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }
        super.computeScroll();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int offsetTop = getOffsetTop();
        int pointerCount = ev.getPointerCount();
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mPointerId = ev.getPointerId(0);
                float x = ev.getX();
                float y = ev.getY();
                tepmY = downY = y;
                downX = x;
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                    if (offsetTop != 0) {
                        setState(SCROLLING, offsetTop);//
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int pointerIndex = ev.findPointerIndex(mPointerId);
                float mx;
                float my;
                if (pointerCount > pointerIndex && pointerIndex > 0) {
                    mx = ev.getX(pointerIndex);
                    my = ev.getY(pointerIndex);
                } else {
                    mx = ev.getX();
                    my = ev.getY();
                }
                float distY = Math.abs(my - downY);
                //意图分析，避免误操作
                if (isScrolling || (distY > mTouchSlop && distY > Math.abs(mx - downX))) {
                    isScrolling = true;
                    int dataY = (int) (my - tepmY);
                    tepmY = my;
                    if (offsetTop == 0) {
                        //开始时 在0,0处
                        //判断是否可以滑动
                        if ((canPullDown() && dataY > 0) || (canPullUp() && dataY < 0)) {

                            setState(SCROLLING, 0);//

                            scrollBy(0, -dataY);

                            return true;
                        }
                    } else {
                        //当不在0,0处
                        ev.setAction(MotionEvent.ACTION_CANCEL);//屏蔽原事件

                        if ((offsetTop < 0 && offsetTop - dataY >= 0) || (offsetTop > 0 && offsetTop - dataY <= 0)) {
                            //在0,0附近浮动
                            ev.setAction(MotionEvent.ACTION_DOWN);
                            scrollTo(0, 0);
                        } else if ((offsetTop > 0 && dataY < 0) || (offsetTop < 0 && dataY > 0)) {
                            //是否超过最大距离
                            if (maxDistance == 0 || Math.abs(offsetTop) < maxDistance) {
                                int ps = 0;
                                int hDataY = dataY / 2;
                                if (maxDistance == 0) {
                                    ps = hDataY + (int) (hDataY * Math.abs(offsetTop) / (float) MAXDISTANCE);
                                } else {
                                    ps = hDataY + (int) (hDataY * Math.abs(offsetTop) / (float) maxDistance);
                                }
                                scrollBy(0, ps - dataY);
                            } else if (offsetTop > maxDistance) {
                                scrollTo(0, maxDistance);
                            } else if (offsetTop < -maxDistance) {
                                scrollTo(0, -maxDistance);
                            }
                        } else {
                            scrollBy(0, -dataY);
                        }
                    }
                } else {
                    ev.setLocation(ev.getX(), tepmY);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                fling(offsetTop);
                isScrolling = false;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                // 获取离开屏幕的手指的索引
                int pointerIndexLeave = ev.getActionIndex();
                int pointerIdLeave = ev.getPointerId(pointerIndexLeave);
                if (mPointerId == pointerIdLeave) {
                    // 离开屏幕的正是目前的有效手指，此处需要重新调整，并且需要重置VelocityTracker
                    int reIndex = pointerIndexLeave == 0 ? 1 : 0;
                    mPointerId = ev.getPointerId(reIndex);
                    // 调整触摸位置，防止出现跳动
                    tepmY = ev.getY(reIndex);
                }
        }
        return super.dispatchTouchEvent(ev) || isScrolling;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
        }
        return super.onTouchEvent(event);
    }

    protected boolean canPullUp() {
        if (mPullView != null) {
            return canPullUp && pullable.isGetBottom();
        }
        return canPullUp;
    }

    protected boolean canPullDown() {
        if (mPullView != null) {
            return canPullDown && pullable.isGetTop();
        }
        return canPullDown;
    }

    protected void fling(int offsetTop) {
        startScrollTo(offsetTop, 0);
    }


    protected void onScroll(int y) {

    }

    protected void onScrollChange(int state, int y) {

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

    public void setMaxDistance(int maxDistance) {
        this.maxDistance = maxDistance;
    }


    public void setCanPullDown(boolean canPullDown) {
        this.canPullDown = canPullDown;
        if (!canPullDown && getOffsetTop() < 0) {
            scrollTo(getScrollX(), 0);
        }
    }

    public void setCanPullUp(boolean canPullUp) {
        this.canPullUp = canPullUp;
        if (!canPullUp && getOffsetTop() > 0) {
            scrollTo(getScrollX(), 0);
        }
    }
}