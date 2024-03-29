package com.imageprocessor.processor.params;

import android.os.Parcel;
import android.os.Parcelable;


import com.imageprocessor.model.FilteringMethod;

import org.opencv.core.Size;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FilteringParams implements Parcelable {
    FilteringMethod filteringMethod;
    Size kernelSize;

    protected FilteringParams(Parcel in) {
        int size = in.readInt();
        kernelSize = new Size(size, size);
        filteringMethod = FilteringMethod.of(in.readInt());
    }


    public static final Creator<FilteringParams> CREATOR = new Creator<FilteringParams>() {
        @Override
        public FilteringParams createFromParcel(Parcel in) {
            return new FilteringParams(in);
        }

        @Override
        public FilteringParams[] newArray(int size) {
            return new FilteringParams[size];
        }
    };

    public FilteringParams(int filteringMethod, int kernelSize) {
        this.filteringMethod = FilteringMethod.of(filteringMethod);
        this.kernelSize = new Size(kernelSize, kernelSize);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt((int) kernelSize.height);
        dest.writeInt(filteringMethod.getValue());
    }
}
