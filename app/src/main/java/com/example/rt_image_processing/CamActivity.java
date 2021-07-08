package com.example.rt_image_processing;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.rt_image_processing.model.ColorSpace;
import com.example.rt_image_processing.model.FilterMode;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class CamActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String  TAG = "MainActivity";

    private BaseLoaderCallback mLoaderCallback;
    private CameraBridgeViewBase mOpenCvCameraView;
    private int mKernelSizeInt;
    private Size mKernelSize;
    private int mHueValue;
    private int mHueRadius;
    private ColorSpace mColorSpace;
    private FilterMode mFilterMode;
    private Mat mCurrentFrame;
    private Mat mThresholdMask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam);
        checkPermissions();
        initializeLoaderCallback();
        initializeMembers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mCurrentFrame = colorSpace(inputFrame);
        filter(mCurrentFrame);
        return threshold(mCurrentFrame);
    }

    private void filter(Mat input) {
        switch (mFilterMode) {
            case AVERAGING: {
                Imgproc.blur(input, input, mKernelSize);
                break;
            }
            case GAUSSIAN: {
                Imgproc.GaussianBlur(input, input, mKernelSize, 0);
                break;
            }
            case MEDIAN: {
                Imgproc.medianBlur(input, input, mKernelSizeInt);
                break;
            }
        }
    }

    private Mat colorSpace(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        switch (mColorSpace) {
            case RGB:
                return inputFrame.rgba();
            case GRAY:
                return inputFrame.gray();
            case HSV:
                mCurrentFrame = inputFrame.rgba();
                Imgproc.cvtColor(mCurrentFrame, mCurrentFrame, Imgproc.COLOR_BGR2HSV);
                break;
        }
        return mCurrentFrame;
    }

    private Mat threshold(Mat input) {
        Mat frameHSV = new Mat();
        Imgproc.cvtColor(input, frameHSV, Imgproc.COLOR_BGR2HSV);
        Mat thresh = new Mat();
        Core.inRange(frameHSV, new Scalar(mHueValue - mHueRadius, 100, 100),
                new Scalar(mHueValue + mHueRadius, 100, 100), thresh);
        return thresh;
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 100);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECORD_AUDIO}, 100);
        }
    }

    private void initializeLoaderCallback() {
        mOpenCvCameraView = findViewById(R.id.camView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        mLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                if (status == LoaderCallbackInterface.SUCCESS) {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } else {
                    super.onManagerConnected(status);
                }
            }
        };
    }

    private void initializeMembers() {
        Intent intent = getIntent();
        mKernelSizeInt = intent.getIntExtra("kernelSize", 3);
        mKernelSize = new Size(mKernelSizeInt, mKernelSizeInt);
        mFilterMode = FilterMode.of(intent.getIntExtra("filterMode", -1));
        mColorSpace = ColorSpace.of(intent.getIntExtra("colorSpace", -1));
    }
}