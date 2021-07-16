package com.example.rt_image_processing.processor;

import com.example.rt_image_processing.model.ColorSpace;
import com.example.rt_image_processing.model.EdgeDetectionMethod;
import com.example.rt_image_processing.model.FilterMode;
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
    private final float mSaturation;
    private final float mSaturationRadius;
    private final float mValue;
    private final float mValueRadius;
    private final float mGrayValue;
    private final float mGrayValueRadius;

    // edge detection
    private EdgeDetectionMethod edgeDetectionMethod;

    // OpenCV objects
    private Mat mHsvFrame;
    private Mat mDownScaledFrame;
    private Mat mThresholdMask;
    private Mat mHierarchy;
    private Scalar mContourColor;

    // util
    private final List<MatOfPoint> mContoursList;
    private Comparator<MatOfPoint> matOfPointComparator;
    private SegmentationMethod mSegmentationMethod;

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

    public void threshold(Mat input) {
        if (mColorSpace == ColorSpace.COLOR) {
            Imgproc.cvtColor(input, mHsvFrame, Imgproc.COLOR_BGR2HSV);
            Core.inRange(mHsvFrame, new Scalar(mHue - mHueRadius, mSaturation - mSaturationRadius, mValue - mValueRadius),
                    new Scalar(mHue + mHueRadius, mSaturation + mSaturationRadius, mValue + mValueRadius), mThresholdMask);
        } else {
            Imgproc.threshold(input, mThresholdMask, mGrayValue - mGrayValueRadius, mGrayValue + mGrayValueRadius, Imgproc.THRESH_BINARY);
        }
    }

    public void findAndDrawContours(Mat input) {

        Imgproc.findContours(mThresholdMask, mContoursList, mHierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
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
        mThresholdMask = new Mat();
        mHierarchy = new Mat();
        mContourColor = new Scalar(0,255,0);
        mDownScaledFrame = new Mat();
        mHsvFrame = new Mat();
        matOfPointComparator = (m1, m2) -> {
            double a = Imgproc.contourArea(m1);
            double b = Imgproc.contourArea(m2);
            return Double.compare(b, a);
        };
    }

    public void freeOpenCvObjects() {
        mHierarchy.release();
        mDownScaledFrame.release();
    }
}
