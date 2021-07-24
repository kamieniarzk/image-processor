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
import com.example.rt_image_processing.model.EdgeDetectionMethod;
import com.example.rt_image_processing.model.FilterMode;
import com.example.rt_image_processing.model.MarkingMethod;
import com.example.rt_image_processing.model.SegmentationMethod;
import com.example.rt_image_processing.processor.ImageProcessor;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String  TAG = "MainActivity";

    private ImageProcessor mImageProcessor;

    private BaseLoaderCallback mLoaderCallback;
    private CameraBridgeViewBase mOpenCvCameraView;

    private Mat mCurrentFrame;

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
        checkPermissions();
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
        mImageProcessor.initializeOpenCvObjects();
    }

    @Override
    public void onCameraViewStopped() {
        mImageProcessor.freeOpenCvObjects();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        System.gc();
        if (mCurrentFrame != null) {
            mCurrentFrame.release();
        }

        mCurrentFrame = mImageProcessor.getMatFromInputFrame(inputFrame);

        mImageProcessor.filter(mCurrentFrame);
        return mImageProcessor.threshold(mCurrentFrame);
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
        int kernelSizeInt = intent.getIntExtra("kernelSize", 0);
        mImageProcessor = ImageProcessor.builder()
                .mKernelSizeInt(kernelSizeInt)
                .mKernelSize(new Size(kernelSizeInt, kernelSizeInt))
                .mFilterMode(FilterMode.of(intent.getIntExtra("filterMode", -1)))
                .mColorSpace(ColorSpace.of(intent.getIntExtra("colorSpace", -1)))
                .mHue(intent.getIntExtra("hue", 0))
                .mHueRadius(intent.getIntExtra("hueRadius", 0))
                .mSaturation(intent.getIntExtra("saturation", 0))
                .mSaturationRadius(intent.getIntExtra("saturationRadius", 0))
                .mValue(intent.getIntExtra("value", 0))
                .mValueRadius(intent.getIntExtra("valueRadius", 0))
                .mGrayValue(intent.getIntExtra("grayValue", 0))
                .mGrayValueRadius(intent.getIntExtra("grayValueRadius", 0))
                .mSegmentationMethod(SegmentationMethod.of(intent.getIntExtra("segmentationMethod", 0)))
                .mEdgeDetectionMethod(EdgeDetectionMethod.of((intent.getIntExtra("segmentationMethod", 0))))
                .mMarkingMethod(MarkingMethod.of(intent.getIntExtra("markingMethod", 0)))
                .mBackgroundRed(intent.getIntExtra("backgroundRed", 0))
                .mBackgroundGreen(intent.getIntExtra("backgroundGreen", 0))
                .mBackgroundBlue(intent.getIntExtra("backgroundBlue", 0))
                .mContoursList(new ArrayList<>())
                .build();
    }
}