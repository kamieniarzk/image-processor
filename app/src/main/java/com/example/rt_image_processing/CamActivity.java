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
import com.example.rt_image_processing.processor.ImageProcessor;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.util.ArrayList;

public class CamActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

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
        mCurrentFrame = mImageProcessor.getMatFromInputFrame(inputFrame);

        mImageProcessor.filter(mCurrentFrame);
        mImageProcessor.threshold(mCurrentFrame);
        mImageProcessor.findAndDrawContours(mCurrentFrame);

        return mCurrentFrame;
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
                .mSaturation(intent.getFloatExtra("saturation", 0))
                .mSaturationRadius(intent.getFloatExtra("saturationRadius", 0))
                .mValue(intent.getFloatExtra("value", 0))
                .mValueRadius(intent.getFloatExtra("valueRadius", 0))
                .mGrayValue(intent.getFloatExtra("grayValue", 0))
                .mGrayValueRadius(intent.getFloatExtra("grayValueRadius", 0))
                .mContoursList(new ArrayList<>())
                .build();
    }
}