package com.bear.librv;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes", "BooleanMethodIsAlwaysInverted"})
public class VHAdapter<VH extends VHolder> extends RecyclerView.Adapter<VH>
        implements LifecycleEventObserver {
    protected String TAG = RvLog.RV_LOG_TAG + "-" + getClass().getSimpleName();
    private static final int DATA_TYPE_LIMIT = 100;
    public static final int DATA_NO_TYPE = -1;
    private LayoutInflater mInflater;
    private RecyclerView mRecyclerView;
    private DataManager mDataManager;
    private Map<Integer, Integer> mDataWithItemTypeMap;
    private SparseArray<VHBridge> mItemTypeWithBridgeMap;
    private int mAutoIncreaseItemType = DATA_TYPE_LIMIT;
    private Context mContext; //通过外部传入好还是onAttachedToRecyclerView拿去
    private Lifecycle mLifecycle;
    private OnDataTypeCreator mOnDataTypeCreator;

    public VHAdapter(Lifecycle lifecycle) {
        mDataManager = new DataManager();
        mDataManager.setAdapter(this);
        mLifecycle = lifecycle;
        mDataWithItemTypeMap = new HashMap<>();
        mItemTypeWithBridgeMap = new SparseArray<>();
    }

    @Override
    public int getItemCount() {
        return mDataManager.size();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mInflater == null) {
            mInflater = LayoutInflater.from(parent.getContext());
        }
        VHolder vh = null;
        VHBridge bridge = mItemTypeWithBridgeMap.get(viewType);
        if (bridge != null) {
            View view = bridge.itemView();
            if (view == null) {
                int layoutId = bridge.layoutId();
                if (layoutId != -1) {
                    view = mInflater.inflate(layoutId, parent, false);
                }
            }
            if (view != null) {
                setUpStaggerFullSpan(view, bridge);
                vh = bridge.onCreateViewHolder(view);
            } else {
                vh = bridge.onCreateViewHolder(parent, viewType);
            }
            vh.attachBridge(bridge);
            if (mLifecycle != null && bridge.isSupportLifecycle()) {
                mLifecycle.addObserver(vh);
            }
        }
        if (vh == null) {
            throw new RuntimeException("VHolder is null");
        }
        return (VH) vh;
    }

    private void setUpStaggerFullSpan(View itemView, VHBridge bridge) {
        if (!bridge.isFullSpan()) {
            return;
        }
        RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
        if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams) itemView.getLayoutParams();
            lp.setFullSpan(true);
            itemView.setLayoutParams(lp);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bindFull(position, mDataManager.get(position));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            for (Object payload : payloads) {
                if (payload instanceof Notify) {
                    holder.bindPartial(mDataManager.get(position), (Notify) payload);
                } else {
                    super.onBindViewHolder(holder, position, payloads);
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        Object data = mDataManager.get(position);
        if (mOnDataTypeCreator != null) {
            int dataType = mOnDataTypeCreator.createDataType(data, position);
            if (dataType != DATA_NO_TYPE) {
                return safeGetItemType(dataType, position);
            }
        }
        if (data instanceof CustomData) {
            return safeGetItemType(((CustomData)data).mType, position);
        }
        if (data instanceof Cursor) {
            return safeGetItemType(Cursor.class.hashCode(), position);
        }
        if (mDataWithItemTypeMap.containsKey(data.getClass().hashCode())) {
            return safeGetItemType(data.getClass().hashCode(), position);
        }
        return super.getItemViewType(position);
    }

    private int safeGetItemType(int dataType, int position) {
        Integer val = mDataWithItemTypeMap.get(dataType);
        return val != null ? val : super.getItemViewType(position);
    }

    /**
     * register bridge with many class
     */
    public void register(VHBridge bridge, Class... clzs) {
        for (Class clz : clzs) {
            if (clz == null) {
                continue;
            }
            int dataType = clz.hashCode();
            registerInternal(bridge, dataType, false);
        }
    }

    /**
     * register bridge with many CustomData
     */
    public void register(VHBridge bridge, CustomData... customDatas) {
        for (CustomData customData : customDatas) {
            if (customData == null) {
                continue;
            }
            registerInternal(bridge, customData.mType, true);
        }
    }
    /**
     * This method should be used in conjunction with the {@link OnDataTypeCreator}
     * @param bridge The VHBridge to be registered
     * @param dataType DataType definition is less than 100
     */
    public void register(VHBridge bridge, int dataType) {
        registerInternal(bridge, dataType, true);
    }

    private void registerInternal(VHBridge bridge, int dataType, boolean fromSetup) {
        if (fromSetup && dataType > DATA_TYPE_LIMIT) {
            Log.w(TAG, "registerInternal: dataType is out of range");
            return;
        }
        if (mDataWithItemTypeMap.containsKey(dataType)) {
            return;
        }
        bridge.onInitAdapterAndManager(this, mDataManager);
        if (mRecyclerView != null && mContext != null) {
            bridge.onInitRvAndContext(mRecyclerView, mContext);
        }
        mAutoIncreaseItemType++;
        bridge.mType = mAutoIncreaseItemType;
        mItemTypeWithBridgeMap.put(mAutoIncreaseItemType, bridge);
        mDataWithItemTypeMap.put(dataType, mAutoIncreaseItemType);
    }

    public boolean isRegister(Object data) {
        if (data == null) {
            return false;
        }
        if (mOnDataTypeCreator != null) {
            int type = mOnDataTypeCreator.createDataType(data, DATA_NO_TYPE);
            if (type != DATA_NO_TYPE) {
                return mDataWithItemTypeMap.containsKey(type);
            }
        }
        if (data instanceof CustomData) {
            return mDataWithItemTypeMap.containsKey(((CustomData)data).mType);
        }
        if (data instanceof Cursor) {
            return mDataWithItemTypeMap.containsKey(Cursor.class.hashCode());
        }
        return mDataWithItemTypeMap.containsKey(data.getClass().hashCode());
    }

    public DataManager getDataManager() {
        return mDataManager;
    }


    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        mContext = recyclerView.getContext();
        if (mLifecycle != null) {
            mLifecycle.addObserver(this);
        }
        for (int i = 0, size = mItemTypeWithBridgeMap.size(); i < size; i++) {
            VHBridge bridge = mItemTypeWithBridgeMap.valueAt(i);
            bridge.onInitRvAndContext(mRecyclerView, mContext);
        }
        setUpGridSpanSize();
    }

    private void setUpGridSpanSize() {
        if (mRecyclerView.getLayoutManager() instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager)mRecyclerView.getLayoutManager();
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                public int getSpanSize(int position) {
                    int type = getItemViewType(position);
                    for (int i = 0, size = mItemTypeWithBridgeMap.size(); i < size; i++) {
                        if (mItemTypeWithBridgeMap.valueAt(i).mType == type) {
                            return mItemTypeWithBridgeMap.valueAt(i).getSpanSize(mRecyclerView);
                        }
                    }
                    return 1;
                }
            });
        }
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            source.getLifecycle().removeObserver(this);
            mInflater = null;
            mRecyclerView = null;
            mDataManager.clear();
            mDataManager = null;
            mDataWithItemTypeMap.clear();
            mDataWithItemTypeMap = null;
            mItemTypeWithBridgeMap.clear();
            mItemTypeWithBridgeMap = null;
            mContext = null;
            mLifecycle = null;
        }
    }

    public interface OnDataTypeCreator {
        int createDataType(Object data, int pos);
    }

    public void setOnDataTypeCreator(OnDataTypeCreator onDataTypeCreator) {
        mOnDataTypeCreator = onDataTypeCreator;
    }
}
