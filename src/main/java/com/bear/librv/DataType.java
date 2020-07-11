package com.bear.librv;

import androidx.annotation.NonNull;

import java.util.Objects;

public class DataType {
    public Object mType;
    public Object mData;

    private DataType(Object type, Object data) {
        mType = type;
        mData = data;
    }

    public static DataType of(@NonNull Object type) {
        return new DataType(type, null);
    }

    public static DataType of(@NonNull Object type, @NonNull Object data) {
        return new DataType(type, data);
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
            return mType.equals(o);
        }
        DataType dataType = (DataType) o;
        return Objects.equals(mType, dataType.mType);
    }
}
