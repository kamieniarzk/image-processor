package com.example.imageprocessor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.imageprocessor.model.ColorSpace;
import com.example.imageprocessor.model.EdgeDetectionMethod;
import com.example.imageprocessor.model.FilteringMethod;
import com.example.imageprocessor.model.MarkingMethod;
import com.example.imageprocessor.model.SegmentationMethod;
import com.example.imageprocessor.model.VideoMetadata;
import com.example.imageprocessor.processor.EdgeDetectionParams;
import com.example.imageprocessor.processor.FilteringParams;
import com.example.imageprocessor.processor.ImageProcessor;
import com.example.imageprocessor.processor.MarkingParams;
import com.example.imageprocessor.processor.ThresholdingParams;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CameraActivity extends AppCompatActivity
        implements CameraBridgeViewBase.CvCameraViewListener2, MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener {

    private static final String TAG = "CameraActivity";

    private ImageProcessor mImageProcessor;

    private BaseLoaderCallback mLoaderCallback;
    private JavaCameraView mOpenCvCameraView;

    private Mat mCurrentFrame;
    private MediaRecorder mMediaRecorder;
    private boolean mRecording;

    private ImageButton mRecordButton;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    private MarkingMethod mMarkingMethod;
    private FilteringMethod mFilteringMethod;
    private SegmentationMethod mSegmentationMethod;
    private EdgeDetectionMethod mEdgeDetectionMethod;

    private String mCurrentFilePath;
    private String mCurrentFileName;

    private ColorSpace mColorSpace;

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

        @SuppressLint("SimpleDateFormat")
        String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        mCurrentFileName = fileName;
        String fileFormat = ".mp4";
        String mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getPath();
        mCurrentFilePath = mediaPath + File.separator + fileName + fileFormat;

        mMediaRecorder.setOutputFile(mCurrentFilePath);
        mMediaRecorder.setVideoSize(mOpenCvCameraView.getmFrameWidth(), mOpenCvCameraView.getmFrameHeight());

        mMediaRecorder.setOnInfoListener(this);
        mMediaRecorder.setOnErrorListener(this);
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "MediaRecorder failed to prepare.", e);
            finish();
        }
    }

    private void toggleRecording() {
        mRecording = !mRecording;
        if (mRecording) {
            mRecordButton.setImageResource(R.drawable.ic_outline_pause_circle_outline_24);
            initializeMediaRecorder();
            mOpenCvCameraView.setRecorder(mMediaRecorder);
            mImageProcessor.resetTimers();
            mMediaRecorder.start();
        } else {
            mRecordButton.setImageResource(R.drawable.ic_baseline_fiber_manual_record_24);
            mOpenCvCameraView.setRecorder(null);
            mMediaRecorder.stop();
            mMediaRecorder.release();
            new Thread(this::saveJsonWithMetadata).start();
            mMediaRecorder = null;
            Toast.makeText(this, "Recording has been saved.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveJsonWithMetadata() {
        VideoMetadata metadata = VideoMetadata.builder()
                .segmentationMethod(mSegmentationMethod)
                .markingMethod(mMarkingMethod)
                .edgeDetectionMethod(mEdgeDetectionMethod)
                .filteringMethod(mFilteringMethod)
                .colorSpace(mColorSpace)
                .extractionTime(mImageProcessor.getMExtractionTimeElapsed())
                .filteringTime(mImageProcessor.getMFilteringTimeElapsed())
                .segmentationTime(mImageProcessor.getMSegmentationTimeElapsed())
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String mediaPath;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            mediaPath = getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath();
        } else {
            mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getPath();
        }

        String jsonFilePath = mediaPath + File.separator + mCurrentFileName + ".json";
        File jsonFile = new File(jsonFilePath);
        try {
            mapper.writeValue(jsonFile, metadata);
        } catch (IOException e) {
            Toast.makeText(this, "Failed to save file with video metadata.", Toast.LENGTH_LONG).show();
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

        if (requestPermissionLauncher == null) {
            requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                return;
            });
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {
            requestPermissionLauncher.launch(
                    Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {
            requestPermissionLauncher.launch(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
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
        mSegmentationMethod = SegmentationMethod.of(intent.getIntExtra("segmentationMethod", 0));
        EdgeDetectionParams edgeDetectionParams = (EdgeDetectionParams) intent.getParcelableExtra("edgeDetectionParams");
        FilteringParams filteringParams = (FilteringParams) intent.getParcelableExtra("filteringParams");
        MarkingParams markingParams = (MarkingParams) intent.getParcelableExtra("markingParams");
        mMarkingMethod = markingParams.getMarkingMethod();
        mFilteringMethod = filteringParams.getFilteringMethod();
        mEdgeDetectionMethod = edgeDetectionParams.getMethod();
        mColorSpace = ColorSpace.of(intent.getIntExtra("colorSpace", -1));
        mImageProcessor = ImageProcessor.builder()
                .thresholdingParams((ThresholdingParams) intent.getParcelableExtra("thresholdingParams"))
                .edgeDetectionParams(edgeDetectionParams)
                .filteringParams(filteringParams)
                .markingParams(markingParams)
                .mColorSpace(mColorSpace)
                .mSegmentationMethod(mSegmentationMethod)
                .mContoursList(new ArrayList<>())
                .build();
    }

    @Override
    public void onInfo(MediaRecorder mediaRecorder, int i, int i1) {
    }

    @Override
    public void onError(MediaRecorder mediaRecorder, int i, int i1) {
    }
}