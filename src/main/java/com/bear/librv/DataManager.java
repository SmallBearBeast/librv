package com.bear.librv;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to handle specific add, remove, and update operation.
 * Note: Cursor is read-only.
 */
@SuppressWarnings({"unchecked", "rawtypes", "BooleanMethodIsAlwaysInverted"})
public class DataManager {
    private static final String TAG = RvLog.RV_LOG_TAG;

    private List mProviderDataList = new ArrayList();
    private Map<Integer, Cursor> mIndexWithCursorMap = new HashMap<>();
    private VHAdapter mAdapter;

    public void setAdapter(VHAdapter adapter) {
        mAdapter = adapter;
    }

    public void setData(List dataList) {
        if (dataList == null) {
            RvLog.w(TAG, "setData: dataList is null");
            return;
        }
        if (dataList.isEmpty()) {
            RvLog.w(TAG, "setData: dataList is empty");
            return;
        }
        List regDataList = new ArrayList();
        for (Object data : dataList) {
            if (!mAdapter.isRegister(data)) {
                RvLog.w(TAG, "setData: " + data.getClass().getSimpleName() + " is not registered");
            } else {
                regDataList.add(data);
            }
        }
        mProviderDataList.clear();
        mProviderDataList.addAll(regDataList);
        mAdapter.notifyDataSetChanged();
    }

    public void addCursor(int index, Cursor cursor) {
        if (cursor != null && cursor.getCount() == 0) {
            RvLog.w(TAG, "addCursor: cursor count is 0");
            return;
        }
        if (!mAdapter.isRegister(cursor)) {
            RvLog.w(TAG, "addCursor: cursor is not registered");
            return;
        }
        if (!checkIndex(index)) {
            RvLog.w(TAG, "addCursor: index is out of range");
            return;
        }
        mProviderDataList.add(index, cursor);
        mIndexWithCursorMap.put(index, cursor);
        mAdapter.notifyItemRangeInserted(index, cursor.getCount());
    }

    public void addCursorFirst(Cursor cursor) {
        addCursor(0, cursor);
    }

    public void addCursorLast(Cursor cursor) {
        addCursor(size(), cursor);
    }

    public void add(int index, List dataList) {
        if (!checkIndex(index)) {
            RvLog.w(TAG, "add: index is out of range");
            return;
        }
        List regDataList = new ArrayList();
        for (Object data : dataList) {
            if (!mAdapter.isRegister(data)) {
                RvLog.w(TAG, "add: " + data.getClass().getSimpleName() + " is not registered");
            } else {
                regDataList.add(data);
            }
        }
        if (regDataList.isEmpty()) {
            RvLog.w(TAG, "add: regDataList is empty");
            return;
        }
        mProviderDataList.addAll(index, regDataList);
        mAdapter.notifyItemRangeInserted(index, regDataList.size());
    }

    public void add(int index, Object... datas) {
        add(index, Arrays.asList(datas));
    }

    public void addFirst(Object data) {
        add(0, data);
    }

    public void addLast(Object data) {
        add(size(), data);
    }

    public void addFirst(List dataList) {
        add(0, dataList);
    }

    public void addLast(List dataList) {
        add(size(), dataList);
    }

    // TODO: 2019-10-20 need removeCursor and upadteCursor method
    public void remove(Object... datas) {
        if (datas.length > 0) {
            for (Object obj : datas) {
                remove(findIndexInArray(obj), 1);
            }
        }
    }

    public void remove(List dataList) {
        if (!dataList.isEmpty()) {
            for (Object obj : dataList) {
                remove(findIndexInArray(obj), 1);
            }
        }
    }

    public void remove(int index, int num) {
        if (num > 0 && index >= 0 && index + num <= size()) {
            mProviderDataList.subList(index, num + index).clear();
            mAdapter.notifyItemRangeRemoved(index, num);
        }
        resetIndexWithCursorMap();
    }

    public void remove(int index) {
        remove(index, 1);
    }

    public void removeFirst(int num) {
        remove(0, num);
    }

    public void removeLast(int num) {
        remove(mProviderDataList.size() - num, num);
    }

    public void update(int index, Object obj, Notify notify) {
        if (!checkIndex(index)) {
            RvLog.w(TAG, "update: index is out of range");
            return;
        }
        if (obj != null) {
            mProviderDataList.set(index, obj);
        }
        mAdapter.notifyItemChanged(index, notify);
    }

    public void update(Object obj) {
        if (obj != null) {
            update(findIndexInArray(obj), obj, null);
        }
    }

    public void update(Object obj, Notify notify) {
        if (obj != null) {
            update(findIndexInArray(obj), obj, notify);
        }
    }

    public void update(int index, Object obj) {
        update(index, obj, null);
    }

    public void update(int index) {
        update(index, null, null);
    }

    // TODO: 2019-07-16 move notifyItemMoved有问题，先使用notifyItemRangeChanged
    public void move(int fromPos, int toPos) {
        if (!checkIndex(fromPos)) {
            RvLog.w(TAG, "move: fromPos is out of range fromPos = " + fromPos);
            return;
        }
        if (!checkIndex(toPos)) {
            RvLog.w(TAG, "move: toPos is out of range toPos = " + toPos);
            return;
        }
        Object fromData = mProviderDataList.get(fromPos);
        Object toData = mProviderDataList.get(toPos);
        mProviderDataList.set(toPos, fromData);
        mProviderDataList.set(fromPos, toData);
//        mAdapter.notifyItemMoved(fromPos, toPos);
//        //由于move机制需要刷新move范围内的item。
//        mAdapter.notifyItemRangeChanged(fromPos, toPos - fromPos + 1);
        mAdapter.notifyItemRangeChanged(fromPos, toPos - fromPos + 1);
    }

    private int findIndexInArray(Object obj) {
        for (int i = 0, len = mProviderDataList.size(); i < len; i++) {
            if (mProviderDataList.get(i).equals(obj)) {
                return i;
            }
        }
        return -1;
    }

    public int size() {
        int size = mProviderDataList.size();
        if (!mIndexWithCursorMap.isEmpty()) {
            for (HashMap.Entry<Integer, Cursor> entry : mIndexWithCursorMap.entrySet()) {
                size = size + entry.getValue().getCount() - 1;
            }
        }
        return size;
    }

    private void resetIndexWithCursorMap() {
        if (mIndexWithCursorMap.isEmpty()) {
            return;
        }
        mIndexWithCursorMap.clear();
        Object obj;
        for (int i = 0, size = mProviderDataList.size(); i < size; i++) {
            obj = mProviderDataList.get(i);
            if (obj instanceof Cursor) {
                mIndexWithCursorMap.put(i, (Cursor) obj);
            }
        }
    }

    public Object get(int position) {
        if (!mIndexWithCursorMap.isEmpty()) {
            for (HashMap.Entry<Integer, Cursor> entry : mIndexWithCursorMap.entrySet()) {
                Cursor cursor = entry.getValue();
                if (position >= entry.getKey() && position < cursor.getCount() + entry.getKey()) {
                    cursor.moveToPosition(position - entry.getKey());
                    return cursor;
                }
            }
        }
        if (position >= 0 && position < size()) {
            int realPosition = position;
            if (!mIndexWithCursorMap.isEmpty()) {
                for (HashMap.Entry<Integer, Cursor> entry : mIndexWithCursorMap.entrySet()) {
                    Cursor cursor = entry.getValue();
                    if (position > entry.getKey()) {
                        realPosition = realPosition - cursor.getCount() + 1;
                    }
                }
             }
            return mProviderDataList.get(realPosition);
        }
        return null;
    }

    private boolean checkIndex(int index) {
        return index >= 0 && index <= size();
    }

    public List getData() {
        return mProviderDataList;
    }

    public void clear() {
        mProviderDataList.clear();
        mProviderDataList = null;
        for (HashMap.Entry<Integer, Cursor> entry : mIndexWithCursorMap.entrySet()) {
            if (!entry.getValue().isClosed()) {
                entry.getValue().close();
            }
        }
        mIndexWithCursorMap.clear();
        mIndexWithCursorMap = null;
    }
}
