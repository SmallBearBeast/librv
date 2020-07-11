package com.bear.librv;

import android.graphics.Canvas;
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
public class RvDivider extends RecyclerView.ItemDecoration {
    private RecyclerView.LayoutManager mLayoutManager;
    private int mOrientation;
    private int mColor;
    private Drawable mDrawable;
    private Paint mPaint;
    private int mDividerWidth;

    public RvDivider(LinearLayoutManager layoutManager, int dividerWidth, int color){
        this((RecyclerView.LayoutManager)layoutManager, dividerWidth, color, null);
    }

    public RvDivider(LinearLayoutManager layoutManager, int dividerWidth, Drawable drawable){
        this((RecyclerView.LayoutManager)layoutManager, dividerWidth, 0, drawable);
    }


    public RvDivider(GridLayoutManager layoutManager, int dividerWidth){
        this((RecyclerView.LayoutManager)layoutManager, dividerWidth, 0, null);
    }

    public RvDivider(StaggeredGridLayoutManager layoutManager, int dividerWidth){
        this(layoutManager, dividerWidth, 0, null);
    }

    private RvDivider(RecyclerView.LayoutManager layoutManager, int dividerWidth, int color, Drawable drawable){
        mLayoutManager = layoutManager;
        mOrientation = getManagerOrientation();
        mDividerWidth = dividerWidth;
        mColor = color;
        mDrawable = drawable;
        //Only the LinearLayoutManager uses color and dividerWidth.
        if(mLayoutManager instanceof LinearLayoutManager) {
            initPaint();
        }
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(mColor);
    }

    //Note that the GridLayoutManager is a subclass of LinearLayoutManager.
    private int getManagerOrientation(){
        if(mLayoutManager instanceof LinearLayoutManager){
            return ((LinearLayoutManager) mLayoutManager).getOrientation();
        }else if(mLayoutManager instanceof StaggeredGridLayoutManager){
            return ((StaggeredGridLayoutManager) mLayoutManager).getOrientation();
        }
        return RecyclerView.VERTICAL;
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if(mLayoutManager instanceof LinearLayoutManager){
            if(mOrientation == RecyclerView.VERTICAL) {
                for (int i = 1, count = parent.getChildCount(); i < count; i++) {
                    View child = parent.getChildAt(i);
                    if(mDrawable != null){
                        mDrawable.setBounds(child.getLeft(), child.getTop() - mDividerWidth, child.getRight(), child.getTop());
                        mDrawable.draw(c);
                    }else {
                        c.drawRect(child.getLeft(), child.getTop() - mDividerWidth, child.getRight(), child.getTop(), mPaint);
                    }
                }
            }else if(mOrientation == RecyclerView.HORIZONTAL){
                for (int i = 1, count = parent.getChildCount(); i < count; i++) {
                    View child = parent.getChildAt(i);
                    if(mDrawable != null){
                        mDrawable.setBounds(child.getLeft(), child.getTop() - mDividerWidth, child.getRight(), child.getTop());
                        mDrawable.draw(c);
                    }else {
                        c.drawRect(child.getLeft() - mDividerWidth, child.getTop(), child.getLeft(), child.getBottom(), mPaint);
                    }
                }
            }
        }
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if(mLayoutManager instanceof GridLayoutManager){
            GridLayoutManager manager = (GridLayoutManager) mLayoutManager;
            int pos = parent.getChildAdapterPosition(view);
            int spanCount = manager.getSpanCount();
            int spanIndex = manager.getSpanSizeLookup().getSpanIndex(pos, spanCount);
            int spanSize = manager.getSpanSizeLookup().getSpanSize(pos);
            int dividerBase = mDividerWidth / spanCount;
            int dividerReal = dividerBase * spanCount;
            if(mOrientation == RecyclerView.VERTICAL) {
                outRect.left = dividerBase * spanIndex;
                outRect.top = isTop(pos, manager) ? 0 : dividerReal;
                outRect.right = (spanIndex + spanSize) == spanCount ? 0 : dividerBase * (spanCount - spanIndex - 1);
            }else if(mOrientation == RecyclerView.HORIZONTAL){
                outRect.top = dividerBase * spanIndex;
                outRect.left = isTop(pos, manager) ? 0 : dividerReal;
                outRect.bottom = (spanIndex + spanSize) == spanCount ? 0 : spanIndex + dividerBase * (spanCount - spanIndex - 1);
            }
        }else if(mLayoutManager instanceof LinearLayoutManager){
            int pos = parent.getChildAdapterPosition(view);
            if(mOrientation == RecyclerView.VERTICAL){
                outRect.set(0, pos == 0 ? 0 : mDividerWidth, 0, 0);
            }else if(mOrientation == RecyclerView.HORIZONTAL){
                outRect.set(pos == 0 ? 0 : mDividerWidth, 0, 0, 0);
            }
        }else if(mLayoutManager instanceof StaggeredGridLayoutManager){
            StaggeredGridLayoutManager manager = (StaggeredGridLayoutManager) mLayoutManager;
            int pos = parent.getChildAdapterPosition(view);
            int spanCount = manager.getSpanCount();
            int dividerBase = mDividerWidth / spanCount;
            int dividerReal = dividerBase * spanCount;
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            if (lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                int spanIndex = ((StaggeredGridLayoutManager.LayoutParams) lp).getSpanIndex();
                if(mOrientation == RecyclerView.VERTICAL){
                    outRect.left = dividerBase * spanIndex;
                    outRect.top = (pos < spanCount ? 0 : dividerReal);
                    outRect.right = dividerBase * (spanCount - spanIndex - 1);
                }else if(mOrientation == RecyclerView.HORIZONTAL){
                    outRect.top = dividerBase * spanIndex;
                    outRect.left = (pos < spanCount ? 0 : dividerReal);
                    outRect.bottom = dividerBase * (spanCount - spanIndex - 1);
                }
            }
        }
    }

    private boolean isTop(int pos, GridLayoutManager manager) {
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
}
