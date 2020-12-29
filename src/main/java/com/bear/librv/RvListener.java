package com.bear.librv;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * RecyclerView全局点击监听器
 */
public class RvListener extends RecyclerView.SimpleOnItemTouchListener {
    private static final byte TYPE_CLICK = 1;
    private static final byte TYPE_LONG_CLICK = 2;
    private GestureDetector mGestureDetector;
    private OnItemClickListener mListener;

    public static class OnItemClickListener {
        //处理了返回true
        public boolean onItemClick(View view, int position){
            return false;
        }
        public boolean onItemLongClick(View view, int position){
            return false;
        }
    }

    public RvListener(Context context, final RecyclerView recyclerView, OnItemClickListener listener) {
        mListener = listener;

        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            //多次点击反应慢是因为走了双击事件的回调
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                //单击事件
                View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (childView != null && mListener != null) {
                    int pos = recyclerView.getChildLayoutPosition(childView);
                    if(onClick(childView, e.getX(), e.getY(), pos, TYPE_CLICK)){
                        return true;
                    }else {
                        return mListener.onItemClick(childView, pos);
                    }
                }
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                //长按事件
                View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (childView != null && mListener != null) {
                    int pos = recyclerView.getChildLayoutPosition(childView);
                    if(!onClick(childView, e.getX(), e.getY(), pos, TYPE_LONG_CLICK)){
                        mListener.onItemLongClick(childView, recyclerView.getChildLayoutPosition(childView));
                    }
                }
            }
        });
    }

    private boolean onClick(View itemView, float x, float y, int pos, int clickType){
        if(!(itemView instanceof ViewGroup)) {
            return false;
        }
        ViewGroup viewGroup = (ViewGroup) itemView;
        float startX = x - itemView.getX();
        float startY = y - itemView.getY();
        for (int i = 0, count = viewGroup.getChildCount(); i < count; i++) {
            View view = viewGroup.getChildAt(i);
            if(onClick(view, startX, startY, pos, clickType)){
                return true;
            }
            if (clickInView(view, startX, startY)) {
                if(clickType == TYPE_CLICK){
                    return mListener.onItemClick(view, pos);
                }else if(clickType == TYPE_LONG_CLICK){
                    return mListener.onItemLongClick(view, pos);
                }
            }
        }
        return false;
    }

    private boolean clickInView(View child, float x, float y){
        return x >= child.getX()
                && x <= child.getX() + child.getWidth()
                && y >= child.getY()
                && y <= child.getY() + child.getHeight();
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        return mGestureDetector.onTouchEvent(e);
    }
}
