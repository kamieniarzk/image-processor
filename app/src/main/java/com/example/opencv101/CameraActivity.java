package com.example.opencv101;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
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
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import static android.view.View.GONE;

public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private enum CameraMode {
        GRAY, RGB, EDGE, BLUR;
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


    /** Called when the activity is first created. */
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
        blurKernelSizeTextView = findViewById(R.id.blurKernelSizeTextView);
        blurKernelSizeSeekBar = findViewById(R.id.blurKernelSizeSeekBar);
        blurKernelSizeSeekBar.setProgress(1);
        blurKernelSize = 1;
        blurKernelSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
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
        flipCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swapCamera();
            }
        });
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

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.cameraView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    private void changeCameraMode(CameraMode cameraMode) {
        if(cameraMode == CameraMode.GRAY || cameraMode == CameraMode.RGB) {
            cannyThresholdSeekBar1.setVisibility(GONE);
            cannyThresholdSeekBar2.setVisibility(GONE);
            threshold1TextView.setVisibility(GONE);
            threshold2TextView.setVisibility(GONE);
            blurKernelSizeSeekBar.setVisibility(GONE);
            blurKernelSizeTextView.setVisibility(GONE);
        }else if(cameraMode == CameraMode.EDGE) {
            cannyThresholdSeekBar1.setVisibility(View.VISIBLE);
            cannyThresholdSeekBar2.setVisibility(View.VISIBLE);
            threshold1TextView.setVisibility(View.VISIBLE);
            threshold2TextView.setVisibility(View.VISIBLE);
            blurKernelSizeSeekBar.setVisibility(GONE);
            blurKernelSizeTextView.setVisibility(GONE);
        }else if(cameraMode == CameraMode.BLUR) {
            cannyThresholdSeekBar1.setVisibility(GONE);
            cannyThresholdSeekBar2.setVisibility(GONE);
            threshold1TextView.setVisibility(GONE);
            threshold2TextView.setVisibility(GONE);
            blurKernelSizeSeekBar.setVisibility(View.VISIBLE);
            blurKernelSizeTextView.setVisibility(View.VISIBLE);
        }
        this.cameraMode = cameraMode;
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

    }

    @Override
    public void onCameraViewStopped() {

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

            default:
                matFrame = inputFrame.rgba();
        }
        return matFrame;
    }

    private Mat blur(Mat source) {
        Mat destination = new Mat(source.rows(),source.cols(),source.type());
        Imgproc.blur(source, destination, new Size(blurKernelSize, blurKernelSize));
        return destination;
    }


    private Mat cannyDetector(Mat source) {
        Mat destination = new Mat(source.rows(),source.cols(),source.type());
        Imgproc.Canny(source, destination, cannyThreshold1, cannyThreshold2);
        return destination;
    }

    private void swapCamera() {
        mCameraId = mCameraId^1;
        mOpenCvCameraView.disableView();
        mOpenCvCameraView.setCameraIndex(mCameraId);
        mOpenCvCameraView.enableView();
    }

}