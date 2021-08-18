package com.example.imageprocessor.processor;

import android.annotation.SuppressLint;
import android.os.SystemClock;
import android.util.Log;

import com.example.imageprocessor.model.ColorSpace;
import com.example.imageprocessor.model.MarkingMethod;
import com.example.imageprocessor.model.SegmentationMethod;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.Iterator;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Builder
public class ImageProcessor {

    // filtering
    private final ColorSpace mColorSpace;
    private final FilteringParams filteringParams;

    // segmentation
    private ThresholdingParams thresholdingParams;
    private Scalar mHsvScalarLow;
    private Scalar mHsvScalarHi;
    private SegmentationMethod mSegmentationMethod;

    // edge detection
    private EdgeDetectionParams edgeDetectionParams;

    // OpenCV objects
    private Mat mHsvFrame;
    private Mat mDownScaledFrame;
    private Mat mBinaryMask;
    private Mat mRgbMask;
    private Mat mHierarchy;
    private Mat mBackgroundColorMask;
    private Mat mAndedFrame;
    private Mat mFinalFrame;
    private Mat mFilteredFrame;
    private Mat mRgbaFrame;

    // marking
    private Mat mBackgroundColorMat;
    private Mat mNegatedMask;
    private MarkingParams markingParams;
    private final List<MatOfPoint> mContoursList;


    @Getter
    private long mFilteringTimeElapsed;
    @Getter
    private long mSegmentationTimeElapsed;
    @Getter
    private long mExtractionTimeElapsed;

    public Mat getMatFromInputFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if (mColorSpace == ColorSpace.COLOR) {
            return inputFrame.rgba();
        } else {
            return inputFrame.gray();
        }
    }

    public Mat filterStep(Mat input) {
        long initialTime = SystemClock.currentThreadTimeMillis();
        switch (filteringParams.getFilteringMethod()) {
            case Averaging:
                Imgproc.blur(input, mFinalFrame, filteringParams.getKernelSize());
                break;
            case Gaussian:
                Imgproc.GaussianBlur(input, mFilteredFrame, filteringParams.getKernelSize(), 0);
                break;
            case Median:
                Imgproc.medianBlur(input, mFilteredFrame, (int) filteringParams.getKernelSize().height);
                break;
            default:
                return input;
        }
        input.release();
        long totalTime = SystemClock.currentThreadTimeMillis() - initialTime;
        mFilteringTimeElapsed += totalTime;
        return mFilteredFrame;
    }

    public Mat segmentationStep(Mat input) {
        long initial = SystemClock.currentThreadTimeMillis();
        Mat output;
        if (mSegmentationMethod == SegmentationMethod.THRESHOLDING) {
            output = threshold(input);
        } else {
            output = detectEdges(input);
        }
        long total = SystemClock.currentThreadTimeMillis() - initial;
        mSegmentationTimeElapsed += total;
        return output;
    }

    public Mat extractionStep(Mat input) {
        long initial = SystemClock.currentThreadTimeMillis();
        Mat output;
        if (markingParams.getMarkingMethod() == MarkingMethod.DRAW_CONTOURS) {
            output = findAndDrawContours(input);
        } else {
            output = substituteColour(input);
        }
        long total = SystemClock.currentThreadTimeMillis() - initial;
        mExtractionTimeElapsed += total;
        return output;
    }

    private Mat detectEdges(Mat input) {
        Imgproc.Canny(input, mBinaryMask, edgeDetectionParams.getThreshold1(), edgeDetectionParams.getThreshold2());
        return input;
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
            Imgproc.threshold(input, mBinaryMask, thresholdingParams.getGrayLow(),
                    thresholdingParams.getGrayHi(), Imgproc.THRESH_BINARY);
        }
        return input;
    }

    public Mat substituteColour(Mat input) {
        if (mColorSpace == ColorSpace.GRAYSCALE) {
            Imgproc.cvtColor(input, input, Imgproc.COLOR_GRAY2BGRA);
        }

        Imgproc.cvtColor(mBinaryMask, mRgbMask, Imgproc.COLOR_GRAY2RGBA);
        mBinaryMask.release();

        if (mBackgroundColorMat == null) {
            mBackgroundColorMat = new Mat(mRgbMask.size(), mRgbMask.type());
            mBackgroundColorMat.setTo(markingParams.getRgbBackgroundScalar());
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
            if (Imgproc.contourArea(iterator.next()) < markingParams.getContourMinArea() * frameArea) {
                iterator.remove();
            }
        }

        for (int i = 0; i < mContoursList.size(); i++) {
            double contourArea = Imgproc.contourArea(mContoursList.get(i));
            if (contourArea >= markingParams.getContourMinArea() * frameArea) {
                Moments moments = Imgproc.moments(mContoursList.get(i));
                Point contourCenter = new Point(new double[] {
                        moments.get_m10() / moments.get_m00(),
                        moments.get_m01() / moments.get_m00()
                });
                Imgproc.drawContours(input, mContoursList, i, markingParams.getRgbContourScalar(), markingParams.getContourThickness());
                Imgproc.putText(input, String.format("%.2f", contourArea/frameArea), contourCenter, Imgproc.FONT_HERSHEY_SIMPLEX,
                        2, markingParams.getRgbContourScalar(), markingParams.getContourThickness());
            }
        }

        mContoursList.clear();
        return input;
    }

    public void initializeOpenCvObjects() {
        mHsvScalarLow = new Scalar(thresholdingParams.getHueLow(), thresholdingParams.getSaturationLow(), thresholdingParams.getValueLow());
        mHsvScalarHi = new Scalar(thresholdingParams.getHueHi(), thresholdingParams.getSaturationHi(), thresholdingParams.getValueHi());
        mBinaryMask = new Mat();
        mHierarchy = new Mat();
        mDownScaledFrame = new Mat();
        mHsvFrame = new Mat();
        mNegatedMask = new Mat();
        mRgbMask = new Mat();
        mBackgroundColorMask = new Mat();
        mAndedFrame = new Mat();
        mFinalFrame = new Mat();
        mFilteredFrame = new Mat();
    }

    public void freeOpenCvObjects() {
        mBinaryMask.release();
        mRgbMask.release();
        mBackgroundColorMask.release();
        mAndedFrame.release();
        mFinalFrame.release();
        mFilteredFrame.release();
        mHierarchy.release();
        mDownScaledFrame.release();
        mNegatedMask.release();
        mBinaryMask.release();
        mHsvFrame.release();
    }

    public void resetTimers() {
        mSegmentationTimeElapsed = 0;
        mExtractionTimeElapsed = 0;
        mFilteringTimeElapsed = 0;
    }
}
