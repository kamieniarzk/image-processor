package com.imageprocessor.processor;

import android.os.Parcel;
import android.os.Parcelable;

import com.imageprocessor.model.EdgeDetectionMethod;
import com.imageprocessor.model.SobelDirection;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EdgeDetectionParams implements Parcelable {

    EdgeDetectionMethod method;
    double threshold1;
    double threshold2;
    SobelDirection sobelDirection;


    protected EdgeDetectionParams(Parcel in) {
        method = EdgeDetectionMethod.of(in.readInt());
        threshold1 = in.readDouble();
        threshold2 = in.readDouble();
        sobelDirection = SobelDirection.of(in.readInt());
    }

    public static final Creator<EdgeDetectionParams> CREATOR = new Creator<EdgeDetectionParams>() {
        @Override
        public EdgeDetectionParams createFromParcel(Parcel in) {
            return new EdgeDetectionParams(in);
        }

        @Override
        public EdgeDetectionParams[] newArray(int size) {
            return new EdgeDetectionParams[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(method.getValue());
        dest.writeDouble(threshold1);
        dest.writeDouble(threshold2);
        dest.writeInt(sobelDirection.getValue());
    }
}
