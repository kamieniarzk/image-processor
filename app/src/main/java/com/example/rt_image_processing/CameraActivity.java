package com.example.rt_image_processing;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.rt_image_processing.model.ColorSpace;
import com.example.rt_image_processing.model.EdgeDetectionMethod;
import com.example.rt_image_processing.model.FilterMode;
import com.example.rt_image_processing.model.ExtractionMethod;
import com.example.rt_image_processing.model.SegmentationMethod;
import com.example.rt_image_processing.processor.ImageProcessor;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.io.IOException;
import java.util.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class CameraActivity extends AppCompatActivity
        implements CameraBridgeViewBase.CvCameraViewListener2, MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener {

    private static final String  TAG = "MainActivity";

    private ImageProcessor mImageProcessor;

    private BaseLoaderCallback mLoaderCallback;
    private JavaCameraView mOpenCvCameraView;

    private Mat mCurrentFrame;
    private MediaRecorder mMediaRecorder;
    private boolean mRecording;

    private ImageButton mRecordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam);
        checkPermissions();
        initializeLoaderCallback();
        initializeMembers();
    }

    private void initializeMediaRecorder() {
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);

        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setVideoEncoder(profile.videoCodec);
        mMediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);

        String externalStoragePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getPath();

        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        mMediaRecorder.setOutputFile(externalStoragePath + "/" + timeStamp + ".mp4");
        mMediaRecorder.setVideoSize(mOpenCvCameraView.getmFrameWidth(), mOpenCvCameraView.getmFrameHeight());

        mMediaRecorder.setOnInfoListener(this);
        mMediaRecorder.setOnErrorListener(this);
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            Toast.makeText(this, "IOexception thrown", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleRecording() {
        mRecording = !mRecording;
        if (mRecording) {
            mRecordButton.setBackgroundResource(R.drawable.ic_outline_pause_circle_outline_24);
            initializeMediaRecorder();
            mOpenCvCameraView.setRecorder(mMediaRecorder);
            mMediaRecorder.start();
        } else {
            mRecordButton.setBackgroundResource(R.drawable.ic_baseline_fiber_manual_record_24);
            mOpenCvCameraView.setRecorder(null);
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
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
            mOpenCvCameraView.setCameraPermissionGranted();
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

        mCurrentFrame = mImageProcessor.filterStep(mCurrentFrame);
        mCurrentFrame = mImageProcessor.segmentationStep(mCurrentFrame);
        mCurrentFrame = mImageProcessor.extractionStep(mCurrentFrame);

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
                    mOpenCvCameraView.setCameraPermissionGranted();
                }
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @org.jetbrains.annotations.NotNull String[] permissions, @NonNull @org.jetbrains.annotations.NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mOpenCvCameraView.setCameraPermissionGranted();
        }
    }

    private void initializeMembers() {
        mRecordButton = findViewById(R.id.recordButton);
        mRecordButton.setOnClickListener(view -> toggleRecording());
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
                .mExtractionMethod(ExtractionMethod.of(intent.getIntExtra("markingMethod", 0)))
                .mBackgroundRed(intent.getIntExtra("backgroundRed", 0))
                .mBackgroundGreen(intent.getIntExtra("backgroundGreen", 0))
                .mBackgroundBlue(intent.getIntExtra("backgroundBlue", 0))
                .mContourRed(intent.getIntExtra("contourRed", 0))
                .mContourGreen(intent.getIntExtra("contourGreen", 0))
                .mContourBlue(intent.getIntExtra("contourBlue", 0))
                .mContourThickness(intent.getIntExtra("contourThickness", 1))
                .mContourArea(intent.getFloatExtra("contourArea", 0.1f))
                .mContoursList(new ArrayList<>())
                .build();
    }

    @Override
    public void onInfo(MediaRecorder mediaRecorder, int i, int i1) {
        Toast.makeText(this, "info", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(MediaRecorder mediaRecorder, int i, int i1) {
        Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
    }
}