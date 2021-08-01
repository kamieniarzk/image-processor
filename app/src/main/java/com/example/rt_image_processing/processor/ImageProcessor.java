package com.example.rt_image_processing.processor;

import android.annotation.SuppressLint;
import android.util.Log;

import com.example.rt_image_processing.model.ColorSpace;
import com.example.rt_image_processing.model.EdgeDetectionMethod;
import com.example.rt_image_processing.model.ExtractionMethod;
import com.example.rt_image_processing.model.FilterMode;
import com.example.rt_image_processing.model.SegmentationMethod;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.Iterator;
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
    private Mat mFilteredFrame;


    // util
    private final List<MatOfPoint> mContoursList;
    private SegmentationMethod mSegmentationMethod;
    private Mat mRgbaFrame;

    // marking
    private ExtractionMethod mExtractionMethod;
    private int mBackgroundRed;
    private int mBackgroundGreen;
    private int mBackgroundBlue;
    private Mat mBackgroundColorMat;
    private Scalar mRgbBackgroundScalar;
    private Mat mNegatedMask;
    private int mContourRed;
    private int mContourGreen;
    private int mContourBlue;
    private float mContourArea;
    private int mContourThickness;
    private Scalar mRgbContourScalar;


    public Mat getMatFromInputFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if (mColorSpace == ColorSpace.COLOR) {
            return inputFrame.rgba();
        } else {
            return inputFrame.gray();
        }
    }

    public Mat filterStep(Mat input) {
        switch (mFilterMode) {
            case Averaging:
                Imgproc.blur(input, mFinalFrame, mKernelSize);
                break;
            case Gaussian:
                Imgproc.GaussianBlur(input, mFilteredFrame, mKernelSize, 0);
                break;
            case Median:
                Imgproc.medianBlur(input, mFilteredFrame, mKernelSizeInt);
                break;
            default:
                return input;
        }
        input.release();
        return mFilteredFrame;
    }

    public Mat segmentationStep(Mat input) {
        if (mSegmentationMethod == SegmentationMethod.THRESHOLDING) {
            return threshold(input);
        } else {
            return detectEdges(input);
        }
    }

    public Mat extractionStep(Mat input) {
        if (mExtractionMethod == ExtractionMethod.DRAW_CONTOURS) {
            return findAndDrawContours(input);
        } else {
            return substituteColour(input);
        }
    }

    private Mat detectEdges(Mat input) {
        return input; //TODO
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
        return input;
    }

    public Mat substituteColour(Mat input) {
        Imgproc.cvtColor(mBinaryMask, mRgbMask, Imgproc.COLOR_GRAY2RGBA);
        mBinaryMask.release();

        if (mBackgroundColorMat == null) {
            mBackgroundColorMat = new Mat(mRgbMask.size(), mRgbMask.type());
            mBackgroundColorMat.setTo(mRgbBackgroundScalar);
        }

        Core.bitwise_not(mRgbMask, mNegatedMask);

        Core.bitwise_and(input, mNegatedMask, mAndedFrame);
        input.release();
        mNegatedMask.release();

        Core.bitwise_and(mBackgroundColorMat, mRgbMask, mBackgroundColorMask);
        mRgbMask.release();

        Core.add(mAndedFrame, mBackgroundColorMask, mFinalFrame);
        mAndedFrame.release();
        mBackgroundColorMask.release();

        return mFinalFrame;
    }

    @SuppressLint("DefaultLocale")
    public Mat findAndDrawContours(Mat input) {
        Imgproc.findContours(mBinaryMask, mContoursList, mHierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        if (!mContoursList.isEmpty() && mColorSpace == ColorSpace.GRAYSCALE) {
            Imgproc.cvtColor(input, mRgbaFrame, Imgproc.COLOR_GRAY2BGRA);
            input.release();
            input = mRgbaFrame;
        }

        double frameArea = input.width() * input.height();
        Iterator<MatOfPoint> iterator = mContoursList.iterator();

        while(iterator.hasNext()) {
            if (Imgproc.contourArea(iterator.next()) < mContourArea * frameArea) {
                iterator.remove();
            }
        }

        for (int i = 0; i < mContoursList.size(); i++) {
            double contourArea = Imgproc.contourArea(mContoursList.get(i));
            if (contourArea >= mContourArea * frameArea) {
                Moments moments = Imgproc.moments(mContoursList.get(i));
                Point contourCenter = new Point(new double[] {
                        moments.get_m10() / moments.get_m00(),
                        moments.get_m01() / moments.get_m00()
                });
                Imgproc.drawContours(input, mContoursList, i, mRgbContourScalar, mContourThickness);
                Imgproc.putText(input, String.format("%.2f", contourArea/frameArea), contourCenter, Imgproc.FONT_HERSHEY_SIMPLEX, 2, mRgbContourScalar, mContourThickness);

            }
        }

        mContoursList.clear();
        return input;
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
        mFilteredFrame = new Mat();
        mRgbBackgroundScalar = new Scalar(mBackgroundRed, mBackgroundGreen, mBackgroundBlue, 0);
        mRgbContourScalar = new Scalar(mContourRed, mContourGreen, mContourBlue, 0);
    }

    public void freeOpenCvObjects() {
        mHierarchy.release();
        mDownScaledFrame.release();
        mNegatedMask.release();
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
