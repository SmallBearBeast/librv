package com.bear.librv;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class VHBridge<VH extends VHolder> {
    protected String TAG = RvLog.RV_LOG_TAG + "-" + getClass().getSimpleName();
    private Map<String, Object> mExtraMap;
    //VHAdapter和DataManager是在register赋值。
    VHAdapter mAdapter;
    DataManager mDataManager;
    //Context和RecyclerView在onAttachedToRecyclerView有值。
    Context mContext;
    RecyclerView mRecyclerView;
    int mType;

    @NonNull
    protected abstract VH onCreateViewHolder(@NonNull View itemView);

    protected abstract @LayoutRes int layoutId();

    protected View itemView(){
        return null;
    }

    protected VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        return null;
    }

    protected int getSpanSize(RecyclerView rv) {
        return 1;
    }

    protected boolean isSupportLifecycle() {
        return false;
    }

    protected boolean isFullSpan() {
        return false;
    }

    void onInitRvAndContext(RecyclerView rv, Context context) {
        mRecyclerView = rv;
        mContext = context;
    }

    void onInitAdapterAndManager(VHAdapter adapter, DataManager manager) {
        mAdapter = adapter;
        mDataManager = manager;
    }

    protected void put(@NonNull String key, @NonNull Object value) {
        if (mExtraMap == null) {
            mExtraMap = new HashMap<>();
        }
        mExtraMap.put(key, value);
    }

    protected @NonNull <V> V get(@NonNull String key) {
        V value = (V) mExtraMap.get(key);
        if (value == null) {
            throw new RuntimeException("The value from mExtraMap is null");
        }
        return value;
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

    public int getType() {
        return mType;
    }
}