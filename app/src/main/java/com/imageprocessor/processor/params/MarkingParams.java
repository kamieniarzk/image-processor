package com.imageprocessor.processor.params;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.imageprocessor.model.MarkingMethod;

import org.opencv.core.Scalar;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class MarkingParams implements Parcelable {
    MarkingMethod markingMethod;
    int backgroundRed;
    int backgroundGreen;
    int backgroundBlue;
    int contourRed;
    int contourGreen;
    int contourBlue;
    @JsonIgnore
    Scalar rgbBackgroundScalar;
    float contourMinArea;
    int contourThickness;
    @JsonIgnore
    Scalar rgbContourScalar;

    public MarkingParams(MarkingMethod markingMethod, int backgroundRed, int backgroundGreen,
                         int backgroundBlue, int contourRed, int contourGreen, int contourBlue,
                         float contourMinArea, int contourThickness) {
        this.markingMethod = markingMethod;
        this.backgroundRed = backgroundRed;
        this.backgroundGreen = backgroundGreen;
        this.backgroundBlue = backgroundBlue;
        this.contourRed = contourRed;
        this.contourGreen = contourGreen;
        this.contourBlue = contourBlue;
        this.contourMinArea = contourMinArea;
        this.contourThickness = contourThickness;
        rgbBackgroundScalar = null;
        rgbContourScalar = null;
    }

    protected MarkingParams(Parcel in) {
        markingMethod = MarkingMethod.of(in.readInt());
        backgroundRed = in.readInt();
        backgroundGreen = in.readInt();
        backgroundBlue = in.readInt();
        contourRed = in.readInt();
        contourGreen = in.readInt();
        contourBlue = in.readInt();
        contourMinArea = in.readFloat();
        contourThickness = in.readInt();

        rgbBackgroundScalar = new Scalar(backgroundRed, backgroundGreen, backgroundBlue, 0);
        rgbContourScalar = new Scalar(contourRed, contourGreen, contourBlue, 0);
    }

    public static final Creator<MarkingParams> CREATOR = new Creator<MarkingParams>() {
        @Override
        public MarkingParams createFromParcel(Parcel in) {
            return new MarkingParams(in);
        }

        @Override
        public MarkingParams[] newArray(int size) {
            return new MarkingParams[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(markingMethod.getValue());
        dest.writeInt(backgroundRed);
        dest.writeInt(backgroundGreen);
        dest.writeInt(backgroundBlue);
        dest.writeInt(contourRed);
        dest.writeInt(contourGreen);
        dest.writeInt(contourBlue);
        dest.writeFloat(contourMinArea);
        dest.writeInt(contourThickness);
    }
}
