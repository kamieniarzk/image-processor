package com.example.rt_image_processing;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import androidx.core.content.FileProvider;

import com.example.rt_image_processing.model.ColorSpace;
import com.example.rt_image_processing.model.EdgeDetectionMethod;
import com.example.rt_image_processing.model.ExtractionMethod;
import com.example.rt_image_processing.model.FilterMode;
import com.example.rt_image_processing.model.SegmentationMethod;
import com.example.rt_image_processing.processor.ImageProcessor;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Size;

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

//        ContentResolver resolver = getApplicationContext()
//                .getContentResolver();
//
//        Uri videoCollection;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            videoCollection = MediaStore.Video.Media
//                    .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
//        } else {
//            videoCollection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
//        }
//
//        ContentValues videoDetails = new ContentValues();
//        videoDetails.put(MediaStore.Video.Media.DISPLAY_NAME,
//                "video.mp4");
//
//        Uri newVideoUri = resolver
//                .insert(videoCollection, videoDetails);
//        File f = new File(newVideoUri.getPath());
//        ParcelFileDescriptor file;
//
//        try {
//            file = resolver.openFileDescriptor(newVideoUri, "w");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        @SuppressLint("SimpleDateFormat")
//
//
//        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
//        String mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getPath();
//        String fileFormat = ".mp4";
//        String outputFilePath = mediaPath + '/' + timeStamp + fileFormat;
//        File moviesPath = getExternalFilesDir("movies");
//        File movieFile = new File(moviesPath, timeStamp + fileFormat);

//        File moviesPath = getExternalFilesDir("movies");
//        Uri moviesUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", moviesPath);
//        File moviesDir = new File(moviesUri.getPath());
//        File movieFile = new File(moviesDir, "moos.mp4");
        String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".mp4";
        String mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getPath();
        String outputFilePath = mediaPath + File.separator + fileName;
//        Uri mediaStoreUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(MediaStore.Video.Media.DISPLAY_NAME, fileName + ".mp4");
//        contentValues.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
//        Uri videoUri = getContentResolver().insert(mediaStoreUri, contentValues);
//        String videoPath = getRealUriPath(videoUri);

        mMediaRecorder.setOutputFile(outputFilePath);
        mMediaRecorder.setVideoSize(mOpenCvCameraView.getmFrameWidth(), mOpenCvCameraView.getmFrameHeight());

        mMediaRecorder.setOnInfoListener(this);
        mMediaRecorder.setOnErrorListener(this);
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "MediaRecorder failed to prepare.", e);
        }
    }

    private void toggleRecording() {
        mRecording = !mRecording;
        if (mRecording) {
            mRecordButton.setImageResource(R.drawable.ic_outline_pause_circle_outline_24);
            initializeMediaRecorder();
            mOpenCvCameraView.setRecorder(mMediaRecorder);
            mMediaRecorder.start();
        } else {
            mRecordButton.setImageResource(R.drawable.ic_baseline_fiber_manual_record_24);
            mOpenCvCameraView.setRecorder(null);
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
            Toast.makeText(this, "Recording has been saved.", Toast.LENGTH_SHORT).show();
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
        } else {
            requestPermissionLauncher.launch(
                    Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
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
    }

    @Override
    public void onError(MediaRecorder mediaRecorder, int i, int i1) {
    }

    private String getRealUriPath(Uri uri) {

        Cursor cursor = null;
        final String column = MediaStore.MediaColumns.DATA;
        final String[] projection = {
                column
        };

        try {
            cursor = getContentResolver().query(uri, projection, null, null,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
}