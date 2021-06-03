package com.example.rt_image_processing;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import java.util.ArrayList;
import java.util.Random;

import static android.view.View.GONE;

public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private enum CameraMode {
        GRAY, RGB, EDGE, BLUR, MOTION_FLOW
    }

    private static final String  TAG = "MainActivity";
    private CameraBridgeViewBase mOpenCvCameraView;
    private Spinner modeSpinner;
    private CameraMode cameraMode;
    private Button flipCameraButton;
    private int mCameraId;
    private SeekBar cannyThresholdSeekBar1;
    private SeekBar cannyThresholdSeekBar2;
    private double cannyThreshold1;
    private double cannyThreshold2;
    private TextView threshold1TextView;
    private TextView threshold2TextView;
    private SeekBar blurKernelSizeSeekBar;
    private TextView blurKernelSizeTextView;
    private int blurKernelSize;
    private Button resetFeaturesToTrack;
    private BatteryManager bm;

    // FPS counter
    private TextView fpsTextView;
    private int mFPS;
    private long startTime = 0;
    private long currentTime = 1000;

    Scalar[] colors;
    Random rng;
    Mat prevGray, currRgb, currGray, drawingMask;
    MatOfPoint p0MatofPoint;
    MatOfPoint2f prevFeatures, nextFeatures;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 100);
        }

        setContentView(R.layout.activity_camera);
        mCameraId = 0;
        threshold1TextView = findViewById(R.id.threshold1TextView);
        threshold2TextView = findViewById(R.id.threshold2TextView);
        fpsTextView = (TextView) findViewById(R.id.fpsTextView);
        blurKernelSizeTextView = findViewById(R.id.blurKernelSizeTextView);
        blurKernelSizeSeekBar = findViewById(R.id.blurKernelSizeSeekBar);
        blurKernelSize = 1;
        blurKernelSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(i % 2 == 0) {
                    i++;
                }
                blurKernelSize = i;
                blurKernelSizeTextView.setText("Blur kernel size: " + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        modeSpinner = findViewById(R.id.modeSpinner);
        flipCameraButton = findViewById(R.id.flip_camera_button);
        flipCameraButton.setOnClickListener(view -> swapCamera());
        resetFeaturesToTrack = findViewById(R.id.reset_features_flow);
        resetFeaturesToTrack.setOnClickListener(view -> resetMotionFlow());
        cannyThresholdSeekBar1 = findViewById(R.id.cannyThreshold1SeekBar);
        cannyThresholdSeekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                cannyThreshold1 = i;
                threshold1TextView.setText("Threshold 1: " + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        cannyThresholdSeekBar2 = findViewById(R.id.cannyThreshold2SeekBar);
        cannyThresholdSeekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                cannyThreshold2 = i;
                threshold2TextView.setText("Threshold 2: " + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        cannyThresholdSeekBar2 = findViewById(R.id.cannyThreshold2SeekBar);
        final CameraMode[] cameraModes = CameraMode.values();
        ArrayAdapter<CameraMode> modeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cameraModes);
        modeAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        modeSpinner.setAdapter(modeAdapter);
        modeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                changeCameraMode(cameraModes[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        changeCameraMode(CameraMode.RGB);

        bm = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.cameraView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.enableFpsMeter();
    }

    private void changeCameraMode(CameraMode camMode) {
        this.cameraMode = camMode;
        switch (camMode) {
            case GRAY:
            case RGB:
                cannyThresholdSeekBar1.setVisibility(GONE);
                cannyThresholdSeekBar2.setVisibility(GONE);
                threshold1TextView.setVisibility(GONE);
                threshold2TextView.setVisibility(GONE);
                blurKernelSizeSeekBar.setVisibility(GONE);
                blurKernelSizeTextView.setVisibility(GONE);
                resetFeaturesToTrack.setVisibility(GONE);
                break;
            case EDGE:
                cannyThresholdSeekBar1.setVisibility(View.VISIBLE);
                cannyThresholdSeekBar2.setVisibility(View.VISIBLE);
                threshold1TextView.setVisibility(View.VISIBLE);
                threshold2TextView.setVisibility(View.VISIBLE);
                blurKernelSizeSeekBar.setVisibility(GONE);
                blurKernelSizeTextView.setVisibility(GONE);
                resetFeaturesToTrack.setVisibility(GONE);
                break;
            case BLUR:
                cannyThresholdSeekBar1.setVisibility(GONE);
                cannyThresholdSeekBar2.setVisibility(GONE);
                threshold1TextView.setVisibility(GONE);
                threshold2TextView.setVisibility(GONE);
                blurKernelSizeSeekBar.setVisibility(View.VISIBLE);
                blurKernelSizeTextView.setVisibility(View.VISIBLE);
                resetFeaturesToTrack.setVisibility(GONE);
                break;
            case MOTION_FLOW:
                cannyThresholdSeekBar1.setVisibility(GONE);
                cannyThresholdSeekBar2.setVisibility(GONE);
                threshold1TextView.setVisibility(GONE);
                threshold2TextView.setVisibility(GONE);
                blurKernelSizeSeekBar.setVisibility(GONE);
                blurKernelSizeTextView.setVisibility(GONE);
                resetFeaturesToTrack.setVisibility(View.VISIBLE);
        }

    }


    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }


    @Override
    public void onCameraViewStarted(int width, int height) {
        colors = new Scalar[100];
        rng = new Random();
        for(int i = 0 ; i < 100 ; i++) {
            int r = rng.nextInt(256);
            int g = rng.nextInt(256);
            int b = rng.nextInt(256);
            colors[i] = new Scalar(r, g, b);
        }
    }

    @Override
    public void onCameraViewStopped() {
        currGray.release();
        currRgb.release();
        prevGray.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat matFrame;
        switch(cameraMode) {
            case RGB: {
                matFrame = inputFrame.rgba();
                break;
            }
            case GRAY: {
                matFrame = inputFrame.gray();
                break;
            }
            case EDGE: {
                matFrame = cannyDetector(inputFrame.gray());
                break;
            }
            case BLUR: {
                matFrame = inputFrame.rgba();
                Imgproc.blur(matFrame, matFrame, new Size(blurKernelSize, blurKernelSize));
                matFrame = blur(inputFrame.rgba());
                break;
            }
            case MOTION_FLOW:
                matFrame = motionFlow(inputFrame);
                break;

            default:
                matFrame = inputFrame.rgba();
        }
        runOnUiThread(() -> {
            if (currentTime - startTime >= 1000) {
                if(fpsTextView != null) {
                    fpsTextView.setText("FPS: " + mFPS);
                }
                mFPS = 0;
                startTime = System.currentTimeMillis();
            }
            currentTime = System.currentTimeMillis();
            mFPS += 1;
        });

        return matFrame;
    }

    private Mat blur(Mat source) {
        Mat destination = new Mat(source.rows(),source.cols(),source.type());
        Imgproc.GaussianBlur(source, destination, new Size(blurKernelSize, blurKernelSize), 0);
        return destination;
    }

    private Mat cannyDetector(Mat source) {
        Mat destination = new Mat(source.rows(),source.cols(),source.type());
        Imgproc.Canny(source, destination, cannyThreshold1, cannyThreshold2);
        return destination;
    }

    private Mat motionFlow(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        // if on first frame, initialize features to track
        if(prevGray == null) {
            prevGray = inputFrame.gray();
            p0MatofPoint = new MatOfPoint();
            Imgproc.goodFeaturesToTrack(prevGray, p0MatofPoint, 100, 0.3, 7, new Mat(), 7, false, 0.04);
            prevFeatures = new MatOfPoint2f(p0MatofPoint.toArray());
            nextFeatures = new MatOfPoint2f();
            drawingMask = Mat.zeros(inputFrame.rgba().size(), inputFrame.rgba().type());
            return inputFrame.rgba();
        }
        currRgb = inputFrame.rgba();
        currGray = inputFrame.gray();
        MatOfByte status = new MatOfByte();
        MatOfFloat error = new MatOfFloat();
        TermCriteria criteria = new TermCriteria(TermCriteria.COUNT + TermCriteria.EPS, 10,0.03);
        if(!prevFeatures.empty() && prevGray.size().equals(currGray.size())) {
            Video.calcOpticalFlowPyrLK(prevGray, currGray, prevFeatures, nextFeatures, status, error, new Size(15,15),2, criteria);
            byte[] statusArray = status.toArray();
            Point[] prevFeaturesArray = prevFeatures.toArray();
            Point[] nextFeaturesArray = nextFeatures.toArray();
            ArrayList<Point> newPointsToTrack = new ArrayList<>();
            for (int i = 0; i < statusArray.length ; i++) {
                if (statusArray[i] == 1) {
                    newPointsToTrack.add(nextFeaturesArray[i]);
                    Imgproc.line(drawingMask, nextFeaturesArray[i], prevFeaturesArray[i], colors[i], 2);
                    Imgproc.circle(currRgb, nextFeaturesArray[i],5, colors[i],-1);
                }
            }
            Core.add(currRgb, drawingMask, currRgb);
            Point[] newPointsToTrackArray = new Point[newPointsToTrack.size()];
            newPointsToTrackArray = newPointsToTrack.toArray(newPointsToTrackArray);
            prevFeatures = new MatOfPoint2f(newPointsToTrackArray);
        }
        prevGray = currGray.clone();
        return currRgb;
    }

    private void swapCamera() {
        mCameraId = mCameraId^1;
        mOpenCvCameraView.disableView();
        mOpenCvCameraView.setCameraIndex(mCameraId);
        mOpenCvCameraView.enableView();
    }

    private void resetMotionFlow() {
        p0MatofPoint = new MatOfPoint();
        Imgproc.goodFeaturesToTrack(prevGray, p0MatofPoint, 100, 0.3, 7, new Mat(), 7, false, 0.04);
        prevFeatures = new MatOfPoint2f(p0MatofPoint.toArray());
        nextFeatures = new MatOfPoint2f();
        drawingMask = Mat.zeros(prevGray.size(), currRgb.type());
    }
}
