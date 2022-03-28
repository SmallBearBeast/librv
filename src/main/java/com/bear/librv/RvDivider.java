package com.bear.librv;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

// 插入时候分割线不会移动，会有视觉问题，最好是分割线是透明区域，由background来决定颜色。
// 插入删除由于每个itemview范围不一致，导致起始点动画突变，基本无解。建议不要有动画。

/**
 * Provide a divider that supports LinearLayoutManager, GridLayoutManager and StaggeredGridLayoutManager.
 * Divider only show between itemView and itemView, and same direction divider width is equal.
 * You can set up the vertical or horizontal divider width if you want.
 * Note: The only LinearLayoutManager supports color and drawable.
 */
// TODO: 2022/3/20 The width is not correct in GridLayoutManager
public class RvDivider extends RecyclerView.ItemDecoration {
    private int mColor = Color.TRANSPARENT;
    private int mVerticalDividerWidth;
    private int mHorizontalDividerWidth;
    private int mOrientation;
    private Paint mPaint;
    private Drawable mDrawable;
    private RecyclerView.LayoutManager mLayoutManager;

    public RvDivider(RecyclerView.LayoutManager layoutManager, int dividerWidth) {
        this(layoutManager, dividerWidth, dividerWidth, 0, null);
    }

    public RvDivider(LinearLayoutManager layoutManager, int dividerWidth, int color) {
        this(layoutManager, dividerWidth, dividerWidth, color, null);
    }

    public RvDivider(LinearLayoutManager layoutManager, int dividerWidth, Drawable drawable) {
        this(layoutManager, dividerWidth, dividerWidth, 0, drawable);
    }

    public RvDivider(GridLayoutManager layoutManager, int verticalDividerWidth, int horizontalDividerWidth) {
        this(layoutManager, verticalDividerWidth, horizontalDividerWidth, 0, null);
    }

    public RvDivider(StaggeredGridLayoutManager layoutManager, int verticalDividerWidth, int horizontalDividerWidth) {
        this(layoutManager, verticalDividerWidth, horizontalDividerWidth, 0, null);
    }

    private RvDivider(RecyclerView.LayoutManager layoutManager, int verticalDividerWidth, int horizontalDividerWidth, int color, Drawable drawable) {
        mLayoutManager = layoutManager;
        mOrientation = getManagerOrientation();
        mVerticalDividerWidth = verticalDividerWidth;
        mHorizontalDividerWidth = horizontalDividerWidth;
        // Only the LinearLayoutManager supports color and drawable.
        if (isLinearLayoutManager(mLayoutManager)) {
            mColor = color;
            mDrawable = drawable;
            initPaint();
        } else if (color != 0 || drawable != null) {
            throw new RuntimeException("Other LayoutManager(GridLayoutManager or StaggeredGridLayoutManager) can not support color or drawable");
        }
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(mColor);
    }

    // Note that the GridLayoutManager is a subclass of LinearLayoutManager.
    private int getManagerOrientation() {
        if (mLayoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) mLayoutManager).getOrientation();
        } else if (mLayoutManager instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) mLayoutManager).getOrientation();
        }
        return RecyclerView.VERTICAL;
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (isLinearLayoutManager(mLayoutManager)) {
            int dividerWidth = mVerticalDividerWidth != 0 ? mVerticalDividerWidth : mHorizontalDividerWidth;
            if (mOrientation == RecyclerView.VERTICAL) {
                drawVerticalForLinearLayout(dividerWidth, c, parent);
            } else if (mOrientation == RecyclerView.HORIZONTAL) {
                drawHorizontalForLinearLayout(dividerWidth, c, parent);
            }
        }
    }

    private void drawVerticalForLinearLayout(int dividerWidth, Canvas c, RecyclerView parent) {
        for (int i = 1, count = parent.getChildCount(); i < count; i++) {
            View child = parent.getChildAt(i);
            if (mDrawable != null) {
                mDrawable.setBounds(child.getLeft(), child.getTop() - dividerWidth, child.getRight(), child.getTop());
                mDrawable.draw(c);
            } else {
                c.drawRect(child.getLeft(), child.getTop() - dividerWidth, child.getRight(), child.getTop(), mPaint);
            }
        }
    }

    private void drawHorizontalForLinearLayout(int dividerWidth, Canvas c, RecyclerView parent) {
        for (int i = 1, count = parent.getChildCount(); i < count; i++) {
            View child = parent.getChildAt(i);
            if (mDrawable != null) {
                mDrawable.setBounds(child.getLeft(), child.getTop() - dividerWidth, child.getRight(), child.getTop());
                mDrawable.draw(c);
            } else {
                c.drawRect(child.getLeft() - dividerWidth, child.getTop(), child.getLeft(), child.getBottom(), mPaint);
            }
        }
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (mLayoutManager instanceof GridLayoutManager) {
            getItemOffsetsForGridLayoutManager((GridLayoutManager) mLayoutManager, outRect, view, parent);
        } else if (mLayoutManager instanceof LinearLayoutManager) {
            getItemOffsetsForLinearLayoutManager(outRect, view, parent);
        } else if (mLayoutManager instanceof StaggeredGridLayoutManager) {
            getItemOffsetsForStaggeredGridLayoutManager((StaggeredGridLayoutManager) mLayoutManager, outRect, view, parent);
        }
    }

    private void getItemOffsetsForGridLayoutManager(GridLayoutManager manager, Rect outRect, View view, RecyclerView parent) {
        int pos = parent.getChildAdapterPosition(view);
        int spanCount = manager.getSpanCount();
        int spanIndex = manager.getSpanSizeLookup().getSpanIndex(pos, spanCount);
        int spanSize = manager.getSpanSizeLookup().getSpanSize(pos);
        if (mOrientation == RecyclerView.VERTICAL) {
            int dividerBase = mVerticalDividerWidth / spanCount;
            outRect.left = dividerBase * spanIndex;
            outRect.top = isStartInGridLayoutManager(pos, manager) ? 0 : mHorizontalDividerWidth;
            outRect.right = (spanIndex + spanSize) == spanCount ? 0 : dividerBase * (spanCount - spanIndex - 1);
        } else if (mOrientation == RecyclerView.HORIZONTAL) {
            int dividerBase = mHorizontalDividerWidth / spanCount;
            outRect.top = dividerBase * spanIndex;
            outRect.left = isStartInGridLayoutManager(pos, manager) ? 0 : mVerticalDividerWidth;
            outRect.bottom = (spanIndex + spanSize) == spanCount ? 0 : spanIndex + dividerBase * (spanCount - spanIndex - 1);
        }
    }

    private void getItemOffsetsForLinearLayoutManager(Rect outRect, View view, RecyclerView parent) {
        int dividerWidth = mVerticalDividerWidth != 0 ? mVerticalDividerWidth : mHorizontalDividerWidth;
        int pos = parent.getChildAdapterPosition(view);
        if (mOrientation == RecyclerView.VERTICAL) {
            outRect.set(0, pos == 0 ? 0 : dividerWidth, 0, 0);
        } else if (mOrientation == RecyclerView.HORIZONTAL) {
            outRect.set(pos == 0 ? 0 : dividerWidth, 0, 0, 0);
        }
    }

    private void getItemOffsetsForStaggeredGridLayoutManager(StaggeredGridLayoutManager manager, Rect outRect, View view, RecyclerView parent) {
        int pos = parent.getChildAdapterPosition(view);
        int spanCount = manager.getSpanCount();
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp instanceof StaggeredGridLayoutManager.LayoutParams) {
            int spanIndex = ((StaggeredGridLayoutManager.LayoutParams) lp).getSpanIndex();
            if (mOrientation == RecyclerView.VERTICAL) {
                int dividerBase = mVerticalDividerWidth / spanCount;
                outRect.left = dividerBase * spanIndex;
                outRect.top = (pos < spanCount ? 0 : mHorizontalDividerWidth);
                outRect.right = dividerBase * (spanCount - spanIndex - 1);
            } else if (mOrientation == RecyclerView.HORIZONTAL) {
                int dividerBase = mHorizontalDividerWidth / spanCount;
                outRect.top = dividerBase * spanIndex;
                outRect.left = (pos < spanCount ? 0 : mVerticalDividerWidth);
                outRect.bottom = dividerBase * (spanCount - spanIndex - 1);
            }
        }
    }

    private boolean isStartInGridLayoutManager(int pos, GridLayoutManager manager) {
        int spanCount = manager.getSpanCount();
        if (pos >= spanCount) {
            return false;
        }
        int spanSize;
        for (int i = 0; i <= pos; i++) {
            spanSize = manager.getSpanSizeLookup().getSpanSize(i);
            spanCount = spanCount - spanSize;
            if (spanCount < 0) {
                return false;
            }
        }
        return true;
    }

    private boolean isLinearLayoutManager(RecyclerView.LayoutManager layoutManager) {
        return layoutManager instanceof LinearLayoutManager && !(layoutManager instanceof GridLayoutManager);
    }
}
