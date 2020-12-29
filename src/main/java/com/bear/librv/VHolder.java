package com.bear.librv;

import android.content.Context;
import android.database.Cursor;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

@SuppressWarnings({"unchecked", "rawtypes"})
public class VHolder<DATA> extends RecyclerView.ViewHolder implements LifecycleEventObserver, View.OnClickListener {
    protected String TAG = RvLog.RV_LOG_TAG + "-" + getClass().getSimpleName();
    private DATA mData;
    private int mPos;
    private VHAdapter mAdapter;
    private DataManager mDataManager;
    private Context mContext;
    private RecyclerView mRecyclerView;
    private VHBridge mBridge;

    public VHolder(View itemView) {
        super(itemView);
    }

    void attachBridge(VHBridge bridge) {
        mBridge = bridge;
        mAdapter = bridge.mAdapter;
        mDataManager = bridge.mDataManager;
        mContext = bridge.mContext;
        mRecyclerView = bridge.mRecyclerView;
    }

    protected void put(@NonNull String key, @NonNull Object value) {
        mBridge.put(key, value);
    }

    protected @NonNull
    <V> V get(String key) {
        return (V) mBridge.get(key);
    }

    protected <T extends View> T findViewById(@IdRes int id) {
        return itemView.findViewById(id);
    }

    protected void setOnClickListener(@IdRes int... ids) {
        for (int id : ids) {
            View view = findViewById(id);
            if (view != null) {
                view.setOnClickListener(this);
            }
        }
    }

    protected void setOnClickListener(View... views) {
        for (View view : views) {
            if (view != null) {
                view.setOnClickListener(this);
            }
        }
    }

    @CallSuper
    protected void bindFull(int pos, DATA data) {
        mData = data;
        mPos = pos;
        if (data instanceof Cursor) {
            bindCursor(pos, (Cursor) data);
        }
    }

    protected void bindCursor(int pos, Cursor cursor) {

    }

    protected void bindPartial(DATA data, @NonNull Notify obj) {

    }

    @Override
    public void onClick(View v) {

    }


    protected void onCreate() {

    }


    protected void onStart() {

    }


    protected void onResume() {

    }


    protected void onPause() {

    }


    protected void onStop() {

    }

    protected void onDestroy() {

    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        if (event == Lifecycle.Event.ON_CREATE) {
            onCreate();
        } else if (event == Lifecycle.Event.ON_START) {
            onStart();
        } else if (event == Lifecycle.Event.ON_RESUME) {
            onResume();
        } else if (event == Lifecycle.Event.ON_PAUSE) {
            onPause();
        } else if (event == Lifecycle.Event.ON_STOP) {
            onStop();
        } else if (event == Lifecycle.Event.ON_DESTROY) {
            onDestroy();
            mAdapter = null;
            mDataManager = null;
            mContext = null;
            mRecyclerView = null;
            source.getLifecycle().removeObserver(this);
        }
    }

    protected DATA getData() {
        return mData;
    }

    protected int getPos() {
        return mPos;
    }

    protected VHAdapter getAdapter() {
        return mAdapter;
    }

    protected DataManager getDataManager() {
        return mDataManager;
    }

    protected Context getContext() {
        return mContext;
    }

    protected RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    protected VHBridge getBridge() {
        return mBridge;
    }
}
