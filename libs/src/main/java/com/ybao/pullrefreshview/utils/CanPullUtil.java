package com.ybao.pullrefreshview.utils;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.ScrollView;

/**
 * Created by ybao on 16/3/7.
 */
public class CanPullUtil {

    public static Pullable getPullAble(View view) {
        if (view == null) {
            return null;
        }
        if (view instanceof Pullable) {
            return (Pullable) view;
        } else if (view instanceof AbsListView) {
            return new AbsListViewCanPull((AbsListView) view);
        } else if (view instanceof ScrollView) {
            return new ScrollViewCanPull((ScrollView) view);

        } else if (view instanceof RecyclerView) {
            return new RecyclerViewCanPull((RecyclerView) view);

        } else if (view instanceof WebView) {
            return new WebViewCanPull((WebView) view);
        }
        return null;
    }

    private static class AbsListViewCanPull implements Pullable {
        public AbsListViewCanPull(AbsListView absListView) {
            this.absListView = absListView;
        }

        AbsListView absListView;

        @Override
        public boolean isGetTop() {
            if (absListView.getChildCount() == 0) {
                return true;
            } else if (absListView.getFirstVisiblePosition() == 0 && absListView.getChildAt(0).getTop() >= 0) {
                return true;
            } else return false;
        }

        @Override
        public boolean isGetBottom() {
            int firstVisiblePosition = absListView.getFirstVisiblePosition();
            int lastVisiblePosition = absListView.getLastVisiblePosition();
            View view = absListView.getChildAt(lastVisiblePosition - firstVisiblePosition);
            if (absListView.getChildCount() == 0) {
                return true;
            } else if (lastVisiblePosition == (absListView.getCount() - 1)) {
                if (view != null && view.getBottom() <= absListView.getMeasuredHeight())
                    return true;
            }
            return false;
        }
    }

    private static class ScrollViewCanPull implements Pullable {
        public ScrollViewCanPull(ScrollView scrollView) {
            this.scrollView = scrollView;
        }

        ScrollView scrollView;

        @Override
        public boolean isGetTop() {
            if (scrollView.getScrollY() <= 0)
                return true;
            else
                return false;
        }

        @Override
        public boolean isGetBottom() {
            if (scrollView.getChildCount() == 0) {
                return true;
            }
            if (scrollView.getScrollY() >= (scrollView.getChildAt(0).getHeight() - scrollView.getMeasuredHeight()))
                return true;
            else
                return false;
        }
    }


    private static class RecyclerViewCanPull implements Pullable {
        public RecyclerViewCanPull(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
            RecyclerView.LayoutManager layout = recyclerView.getLayoutManager();
            if (layout != null && layout instanceof LinearLayoutManager) {
                layoutManager = (LinearLayoutManager) layout;
            }
        }

        RecyclerView recyclerView;
        LinearLayoutManager layoutManager;


        @Override
        public boolean isGetTop() {
            if (layoutManager != null) {
                int count = layoutManager.getItemCount();
                if (count == 0) {
                    return true;
                } else if (layoutManager.findFirstVisibleItemPosition() == 0 && recyclerView.getChildAt(0).getTop() >= 0) {
                    return true;
                }

            }
            return false;
        }

        @Override
        public boolean isGetBottom() {

            int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
            int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
            if (layoutManager != null) {
                int count = layoutManager.getItemCount();
                if (count == 0) {
                    return true;
                } else if (lastVisiblePosition == count - 1
                        && recyclerView.getChildAt(
                        lastVisiblePosition - firstVisiblePosition)
                        .getBottom() <= recyclerView.getMeasuredHeight()) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class WebViewCanPull implements Pullable {
        public WebViewCanPull(WebView webView) {
            this.webView = webView;
        }

        WebView webView;

        @Override
        public boolean isGetBottom() {
            if (webView.getScrollY() >= webView.getContentHeight() * webView.getScale() - webView.getMeasuredHeight())
                return true;
            else
                return false;
        }

        @Override
        public boolean isGetTop() {
            if (webView.getScrollY() <= 0)
                return true;
            else
                return false;
        }
    }

}
