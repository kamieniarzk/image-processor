package com.example.rt_image_processing.processor;

import android.util.Log;

import com.example.rt_image_processing.model.ColorSpace;
import com.example.rt_image_processing.model.EdgeDetectionMethod;
import com.example.rt_image_processing.model.FilterMode;
import com.example.rt_image_processing.model.MarkingMethod;
import com.example.rt_image_processing.model.SegmentationMethod;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lombok.Builder;

@Builder
public class ImageProcessor {

    // filtering
    private final ColorSpace mColorSpace;
    private final int mKernelSizeInt;
    private final Size mKernelSize;
    private final FilterMode mFilterMode;

    // thresholding
    private final int mHue;
    private final int mHueRadius;
    private final int mSaturation;
    private final int mSaturationRadius;
    private final int mValue;
    private final int mValueRadius;
    private final int mGrayValue;
    private final int mGrayValueRadius;
    private int mGrayLow;
    private int mGrayHi;
    private Scalar mHsvScalarLow;
    private Scalar mHsvScalarHi;

    // edge detection
    private EdgeDetectionMethod mEdgeDetectionMethod;

    // OpenCV objects
    private Mat mHsvFrame;
    private Mat mDownScaledFrame;
    private Mat mBinaryMask;
    private Mat mRgbMask;
    private Mat mHierarchy;
    private Mat mBackgroundColorMask;
    private Mat mAndedFrame;
    private Mat mFinalFrame;
    private Scalar mContourColor;


    // util
    private final List<MatOfPoint> mContoursList;
    private Comparator<MatOfPoint> matOfPointComparator;
    private SegmentationMethod mSegmentationMethod;

    // marking
    private MarkingMethod mMarkingMethod;
    private int mBackgroundRed;
    private int mBackgroundGreen;
    private int mBackgroundBlue;
    private Mat mBackgroundColorMat;
    private Scalar mRgbBackgroundScalar;
    private Mat mNegatedMask;

    public Mat getMatFromInputFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if (mColorSpace == ColorSpace.COLOR) {
            return inputFrame.rgba();
        } else {
            return inputFrame.gray();
        }
    }

    public void filter(Mat input) {
        switch (mFilterMode) {
            case Averaging: {
                Imgproc.blur(input, input, mKernelSize);
                break;
            }
            case Gaussian: {
                Imgproc.GaussianBlur(input, input, mKernelSize, 0);
                break;
            }
            case Median: {
                Imgproc.medianBlur(input, input, mKernelSizeInt);
                break;
            }
        }
    }

    public void segmentationStep(Mat input) {
        if (mSegmentationMethod == SegmentationMethod.THRESHOLDING) {
            threshold(input);
        } else {
            detectEdges(input);
        }
    }

    private void detectEdges(Mat input) {

    }

    public Mat threshold(Mat input) {
        if (mFinalFrame != null) {
            mFinalFrame.release();
        }

        if (mColorSpace == ColorSpace.COLOR) {
            Imgproc.cvtColor(input, mHsvFrame, Imgproc.COLOR_RGB2HSV);
            Core.inRange(mHsvFrame, mHsvScalarLow, mHsvScalarHi, mBinaryMask);
            mHsvFrame.release();
            Log.i("Lower Hsv", mHsvScalarLow.toString());
            Log.i("Hi Hsv", mHsvScalarHi.toString());
        } else {
            Imgproc.threshold(input, mBinaryMask, mGrayLow, mGrayHi, Imgproc.THRESH_BINARY);
        }
        Imgproc.cvtColor(mBinaryMask, mRgbMask, Imgproc.COLOR_GRAY2RGBA);
        mBinaryMask.release();

        mBackgroundColorMat = new Mat(mRgbMask.size(), mRgbMask.type());

        Core.bitwise_not(mRgbMask, mNegatedMask);
        mBackgroundColorMat.setTo(mRgbBackgroundScalar);

        Core.bitwise_and(input, mNegatedMask, mAndedFrame);
        input.release();
        mNegatedMask.release();

        Core.bitwise_and(mBackgroundColorMat, mRgbMask, mBackgroundColorMask);
        mRgbMask.release();
        mBackgroundColorMat.release();

        Core.add(mAndedFrame, mBackgroundColorMask, mFinalFrame);
        mAndedFrame.release();
        mBackgroundColorMask.release();

        return mFinalFrame;
    }

    public void findAndDrawContours(Mat input) {

        Imgproc.findContours(mBinaryMask, mContoursList, mHierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        Collections.sort(mContoursList, matOfPointComparator);
        if (!mContoursList.isEmpty() && mColorSpace == ColorSpace.GRAYSCALE) {
            Imgproc.cvtColor(input, input, Imgproc.COLOR_GRAY2BGR);
        }
        for (int i = 0; i < 2 && i < mContoursList.size(); i++) {
            Imgproc.drawContours(input, mContoursList, i, mContourColor, 10);
        }

        mContoursList.clear();
    }

    public void initializeOpenCvObjects() {
        int hueLow = normalizeInRangeFromZero(mHue - mHueRadius, 179);
        int hueHi = normalizeInRangeFromZero(mHue + mHueRadius, 179);
        int satLow = normalizeInRangeFromZero(mSaturation - mSaturationRadius, 255);
        int satHi = normalizeInRangeFromZero(mSaturation + mSaturationRadius, 255);
        int valLow = normalizeInRangeFromZero(mValue - mValueRadius, 255);
        int valHi = normalizeInRangeFromZero(mValue + mValueRadius, 255);
        mGrayLow = normalizeInRangeFromZero(mGrayValue - mSaturationRadius, 255);
        mGrayHi = normalizeInRangeFromZero(mGrayValue + mGrayValueRadius, 255);

        mHsvScalarLow = new Scalar(hueLow, satLow, valLow);
        mHsvScalarHi = new Scalar(hueHi, satHi, valHi);
        mBinaryMask = new Mat();
        mHierarchy = new Mat();
        mContourColor = new Scalar(0,255,0);
        mDownScaledFrame = new Mat();
        mHsvFrame = new Mat();
        mNegatedMask = new Mat();
        mRgbMask = new Mat();
        mBackgroundColorMask = new Mat();
        mAndedFrame = new Mat();
        mFinalFrame = new Mat();
        matOfPointComparator = (m1, m2) -> {
            double a = Imgproc.contourArea(m1);
            double b = Imgproc.contourArea(m2);
            return Double.compare(b, a);
        };
        mRgbBackgroundScalar = new Scalar(mBackgroundRed, mBackgroundGreen, mBackgroundRed);
    }

    public void freeOpenCvObjects() {
        mHierarchy.release();
        mDownScaledFrame.release();
        mNegatedMask.release();
        mBackgroundColorMat.release();
        mBinaryMask.release();
        mHsvFrame.release();
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
