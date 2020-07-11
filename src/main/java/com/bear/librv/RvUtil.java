package com.bear.librv;

import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

//1.RecyclerView.getChildCount()与LayoutManager.getChildCount()输出相同表示ViewGroup里面的View数目，也就是attachView数目，一般不改写getExtraLayoutSpace也就是可见数目。
//2.LayoutManager.getItemCount()表示所有item数目。
//3.findLastVisibleItemPosition(layoutManager)表示最后一个可见item的index，可见范围包括Decoration。
//4.findFirstVisibleItemPosition(layoutManager)表示第一个可见item的index，可见范围包括Decoration。
//5.findFirstCompletelyVisibleItemPosition(layoutManager)表示第一个完全可见item的index，可见范围包括Decoration，若无返回-1。
//6.findLastCompletelyVisibleItemPosition(layoutManager)表示最后一个完全可见item的index，可见范围包括Decoration，若无返回-1。
//7.RecyclerView.computeVerticalScrollOffset()和computeVerticalScrollRange()内部取平均值计算，对于高度变化的item不准确。
//8.RecyclerView.canScrollVertically()原理基于7，因此高度变化的item判断也是不准确的。
//9.RecyclerView.computeVerticalScrollExtent获取到的是去除掉padding的大小，见ScrollBarHelper。
//10.scrollBy间隔太多会有卡顿问题。
public class RvUtil {
    private static final String TAG = "RvUtil";

    public static void scrollToTop(final RecyclerView rv, int limitRange, int offset){
        int first = findFirstVisibleItemPosition(rv);
        if(first < limitRange){
            scrollToTop(rv, true);
        }else {
            scrollToPos(rv, limitRange, false, offset);
            rv.post(new Runnable() {
                @Override
                public void run() {
                    scrollToTop(rv, true);
                }
            });
        }
    }

    public static void scrollToTop(RecyclerView rv, boolean smooth) {
        scrollToPos(rv, 0, smooth, 0);
    }

    public static void scrollToBottom(final RecyclerView rv, int limitRange, int offset){
        int last = findLastVisibleItemPosition(rv);
        int itemCount = rv.getLayoutManager().getItemCount();
        if(last >= itemCount - limitRange){
            scrollToBottom(rv, true);
        }else {
            scrollToPos(rv, itemCount - limitRange, false, offset);
            rv.post(new Runnable() {
                @Override
                public void run() {
                    scrollToBottom(rv, true);
                }
            });
        }
    }

    public static void scrollToBottom(RecyclerView rv, boolean smooth) {
        RecyclerView.LayoutManager layoutManager = rv.getLayoutManager();
        scrollToPos(rv, layoutManager.getItemCount() - 1, smooth, 0);
    }

    /**
     * offset是偏移量，只有smooth为false，offset才会有起作用。
     * offest > 0 内容向下向右滚动。
     */
    public static void scrollToPos(RecyclerView rv, int pos, boolean smooth, int offset) {
        RecyclerView.LayoutManager layoutManager = rv.getLayoutManager();
        if (pos < 0 || pos >= layoutManager.getItemCount()) {
            return;
        }
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager manager = (LinearLayoutManager) layoutManager;
            if (smooth) {
                TopLeftAlignScroller scroller = new TopLeftAlignScroller(rv.getContext());
                scroller.setTargetPosition(pos);
                manager.startSmoothScroll(scroller);
            } else {
                manager.scrollToPositionWithOffset(pos, offset);
            }
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager manager = (StaggeredGridLayoutManager) layoutManager;
            if (smooth) {
                TopLeftAlignScroller scroller = new TopLeftAlignScroller(rv.getContext());
                scroller.setTargetPosition(pos);
                manager.startSmoothScroll(scroller);
            } else {
                manager.scrollToPositionWithOffset(pos, offset);
            }
        }
    }

    private static class TopLeftAlignScroller extends LinearSmoothScroller {
        TopLeftAlignScroller(Context context) {
            super(context);
        }

        @Override
        protected int getHorizontalSnapPreference() {
            return SNAP_TO_START;
        }

        @Override
        protected int getVerticalSnapPreference() {
            return SNAP_TO_START;
        }
    }

    public static int findFirstVisibleItemPosition(RecyclerView rv) {
        RecyclerView.LayoutManager manager = rv.getLayoutManager();
        if (manager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) manager).findFirstVisibleItemPosition();
        } else if (manager instanceof StaggeredGridLayoutManager) {
            int[] info = ((StaggeredGridLayoutManager) manager).findFirstVisibleItemPositions(null);
            if (info == null || info.length <= 0) {
                return -1;
            }
            return min(info);
        }
        return -1;
    }

    public static int findLastVisibleItemPosition(RecyclerView rv) {
        RecyclerView.LayoutManager manager = rv.getLayoutManager();
        if (manager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) manager).findLastVisibleItemPosition();
        } else if (manager instanceof StaggeredGridLayoutManager) {
            int[] into = ((StaggeredGridLayoutManager) manager).findLastVisibleItemPositions(null);
            if (into == null || into.length <= 0) {
                return -1;
            }
            return max(into);
        }
        return -1;
    }

    public static int findFirstCompletelyVisibleItemPosition(RecyclerView rv){
        RecyclerView.LayoutManager manager = rv.getLayoutManager();
        if (manager instanceof StaggeredGridLayoutManager) {
            int[] info = ((StaggeredGridLayoutManager) manager).findFirstCompletelyVisibleItemPositions(null);
            if (info == null || info.length <= 0) {
                return -1;
            }
            return min(info);
        } else if (manager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) manager).findFirstCompletelyVisibleItemPosition();
        }
        return -1;
    }

    public static int findLastCompletelyVisibleItemPosition(RecyclerView rv){
        RecyclerView.LayoutManager manager = rv.getLayoutManager();
        if (manager instanceof StaggeredGridLayoutManager) {
            int[] info = ((StaggeredGridLayoutManager) manager).findLastCompletelyVisibleItemPositions(null);
            if (info == null || info.length <= 0) {
                return -1;
            }
            return max(info);
        } else if (manager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) manager).findLastCompletelyVisibleItemPosition();
        }
        return -1;
    }

    public static boolean isTop(RecyclerView rv){
        int completeFirst = findFirstCompletelyVisibleItemPosition(rv);
        int visibleFirst = findFirstVisibleItemPosition(rv);
        if(completeFirst == 0){
            return true;
        }else if(completeFirst == -1 && visibleFirst > 0){
            return false;
        }else if(completeFirst == -1 && visibleFirst == 0){
            View view = firstView(rv);
            return view != null && view.getTop() == 0;
        }
        return false;
    }

    public static boolean isBottom(RecyclerView rv){
        RecyclerView.LayoutManager manager = rv.getLayoutManager();
        int completeLast = findLastCompletelyVisibleItemPosition(rv);
        int visibleLast = findLastVisibleItemPosition(rv);
        if(completeLast == manager.getItemCount() - 1){
            return true;
        }else if(completeLast == -1 && visibleLast < manager.getItemCount() - 1){
            return false;
        }else if(completeLast == -1 && visibleLast == manager.getItemCount() - 1){
            View view = lastView(rv);
            return view != null && view.getBottom() == rv.computeVerticalScrollExtent();
        }
        return false;
    }

    private static View firstView(RecyclerView rv){
        int first = findFirstVisibleItemPosition(rv);
        return rv.getLayoutManager().findViewByPosition(first);
    }

    private static View lastView(RecyclerView rv){
        int last = findLastVisibleItemPosition(rv);
        return rv.getLayoutManager().findViewByPosition(last);
    }

    public static void test(RecyclerView rv, RecyclerView.LayoutManager manager){
        Log.d(TAG, "test: findLastCompletelyVisibleItemPosition(manager) = " + findLastCompletelyVisibleItemPosition(rv));
        Log.d(TAG, "test: findFirstCompletelyVisibleItemPosition(manager) = " + findFirstCompletelyVisibleItemPosition(rv));
        Log.d(TAG, "test: isTop() = " + isTop(rv));
        Log.d(TAG, "test: isBottom() = " + isBottom(rv));
        Log.d(TAG, "test: rv.computeVerticalScrollOffset() = " + rv.computeVerticalScrollOffset());
        Log.d(TAG, "test: rv.computeVerticalScrollRange() = " + rv.computeVerticalScrollRange());
        Log.d(TAG, "test: rv.computeVerticalScrollExtent() = " + rv.computeVerticalScrollExtent());
        Log.d(TAG, "test: rv.canScrollVertically(1) = " + rv.canScrollVertically(1));
        Log.d(TAG, "test: rv.canScrollVertically(-1) = " + rv.canScrollVertically(-1));
    }

    public static void scrollToPos(final RecyclerView rv, final int pos, int limitRange, final int offset){
        int first = findFirstVisibleItemPosition(rv);
        int diff = Math.abs(pos - first);
        if(diff <= limitRange){
            scrollToPos(rv, pos, true, offset);
        }else {
            if(first > pos){
                scrollToPos(rv, pos + limitRange, false, offset);
                rv.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollToPos(rv, pos, true, offset);
                    }
                });
            }else {
                scrollToPos(rv, pos - limitRange, false, offset);
                rv.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollToPos(rv, pos, true, offset);
                    }
                });
            }
        }
    }

    private static int max(int ... array) {
        int max = array[0];
        for(int i = 1; i < array.length; ++i) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    private static int min(int ... array) {
        int min = array[0];
        for(int i = 1; i < array.length; ++i) {
            if (array[i] < min) {
                min = array[i];
            }
        }
        return min;
    }
}
