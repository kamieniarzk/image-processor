package com.example.imageprocessor.processor;

import android.os.Parcel;
import android.os.Parcelable;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ThresholdingParams implements Parcelable {

    int hueLow;
    int hueHi;
    int saturationLow;
    int saturationHi;
    int valueLow;
    int valueHi;
    int grayLow;
    int grayHi;

    public ThresholdingParams(int hue, int hueRadius, float saturation, float saturationRadius,
                              float value, float valueRadius, int grayValue, int grayValueRadius) {
        this.hueLow = normalizeInRangeFromZero(hue - hueRadius, 179);
        this.hueHi = normalizeInRangeFromZero(hue + hueRadius, 179);
        int intSaturation = convertFrom1To255Range(saturation);
        int intSaturationRadius = convertFrom1To255Range(saturationRadius);
        int intValue = convertFrom1To255Range(value);
        int intValueRadius = convertFrom1To255Range(valueRadius);
        this.saturationLow = normalizeInRangeFromZero(intSaturation - intSaturationRadius, 255);
        this.saturationHi = normalizeInRangeFromZero(intSaturation + intSaturationRadius, 255);
        this.valueLow = normalizeInRangeFromZero(intValue - intValueRadius, 255);
        this.valueHi = normalizeInRangeFromZero(intValue + intValueRadius, 255);
        this.grayLow = normalizeInRangeFromZero(grayValue - grayValueRadius, 255);
        this.grayHi = normalizeInRangeFromZero(grayValue + grayValueRadius, 255);
    }

    protected ThresholdingParams(Parcel in) {
        hueLow = in.readInt();
        hueHi = in.readInt();
        saturationLow = in.readInt();
        saturationHi = in.readInt();
        valueLow = in.readInt();
        valueHi = in.readInt();
        grayLow = in.readInt();
        grayHi = in.readInt();
    }

    public static final Creator<ThresholdingParams> CREATOR = new Creator<ThresholdingParams>() {
        @Override
        public ThresholdingParams createFromParcel(Parcel in) {
            return new ThresholdingParams(in);
        }

        @Override
        public ThresholdingParams[] newArray(int size) {
            return new ThresholdingParams[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(hueLow);
        dest.writeInt(hueHi);
        dest.writeInt(saturationLow);
        dest.writeInt(saturationHi);
        dest.writeInt(valueLow);
        dest.writeInt(valueHi);
        dest.writeInt(grayLow);
        dest.writeInt(grayHi);
    }

    private int convertFrom1To255Range(float input) {
        return (int) (input * 255);
    }

    private int normalizeInRangeFromZero(int value, int range) {
        if (value < 0) {
            return 0;
        } else if (value > range) {
            return range;
        }
        return value;
    }
}
