package com.bear.librv;

import androidx.annotation.NonNull;

public class CustomData {
    public int mType;
    public Object mData;

    private CustomData(int type, Object data) {
        mType = type;
        mData = data;
    }

    public static CustomData of(int type) {
        return new CustomData(type, null);
    }

    public static CustomData of(int type, @NonNull Object data) {
        return new CustomData(type, data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        CustomData customData = (CustomData) o;
        return mType == customData.mType;
    }
}
