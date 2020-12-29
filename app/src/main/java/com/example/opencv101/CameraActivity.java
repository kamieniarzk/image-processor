package com.example.opencv101;

import androidx.appcompat.app.AppCompatActivity;
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
import android.widget.Spinner;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private enum CameraMode {
        GRAY, RGB, EDGE;
    }

    private static final String  TAG = "MainActivity";
    private CameraBridgeViewBase mOpenCvCameraView;
    private Spinner modeSpinner;
    private CameraMode cameraMode;
    private int mCameraId;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
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
        cameraMode = CameraMode.RGB;

        modeSpinner = findViewById(R.id.modeSpinner);
        final CameraMode[] cameraModes = CameraMode.values();
        ArrayAdapter<CameraMode> modeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cameraModes);
        modeAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        modeSpinner.setAdapter(modeAdapter);
        modeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                cameraMode = cameraModes[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });




        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.cameraView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
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
                matFrame = cannyDetector(inputFrame.gray(), Filters.SOBEL_X.filter, Filters.SOBEL_Y.filter);
                break;
            }
            default:
                matFrame = inputFrame.rgba();
        }
        return matFrame;
    }

    private Mat cannyDetector(Mat source, int[][] filterX, int[][] filterY) {
        Mat destination = new Mat(source.rows(),source.cols(),source.type());
        Imgproc.Canny(source, destination, 100, 80);
        return destination;
    }

    private void swapCamera() {
        mCameraId = mCameraId^1; //bitwise not operation to flip 1 to 0 and vice versa
        mOpenCvCameraView.disableView();
        mOpenCvCameraView.setCameraIndex(mCameraId);
        mOpenCvCameraView.enableView();
    }

}