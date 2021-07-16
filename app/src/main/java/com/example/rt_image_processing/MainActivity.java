package com.example.rt_image_processing;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rt_image_processing.model.ColorSpace;
import com.example.rt_image_processing.model.EdgeDetectionMethod;
import com.example.rt_image_processing.model.FilterMode;
import com.example.rt_image_processing.model.SegmentationMethod;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.slider.Slider;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final List<Integer> KERNEL_SIZES = List.of(3, 5, 7, 9);
    private static final int[] KERNEL_TYPES = new int[]{R.string.averaging, R.string.gaussian, R.string.median};
    private TextView mFilterDescriptionTextView;
    private LinearLayout mFilterSizeSelectorLayout;
    private LinearLayout mHsvSlidersLayout;
    private LinearLayout mGraySlidersLayout;
    private MaterialCardView mThresholdingCardview;
    private MaterialCardView mEdgeDetectionCardView;
    private MaterialToolbar mToolbar;
    private int mKernelSize;
    private ColorSpace mColorSpace;
    private FilterMode mFilterMode;
    private SegmentationMethod mSegmentationMethod;
    private EdgeDetectionMethod mEdgeDetectionMethod;
    private float mGrayValue;
    private float mGrayValueRadius;
    private float mHue;
    private float mSaturation;
    private float mValue;
    private float mHueRadius;
    private float mValueRadius;
    private float mSaturationRadius;
    private GradientDrawable mGradientDrawable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeLayout();
    }

    private void initializeLayout() {
        initializeButtons();
        initializeFilterModeCardView();
        initializeKernelSizeSpinner();
        initializeGradientView();
        initializeHsvSliders();
        initializeGraySliders();
        initializeColorButton();
        initializeSegmentationLayout();
        initializeEdgeDetectionLayout();
    }

    private void initializeEdgeDetectionLayout() {
        RadioButton sobelButton = findViewById(R.id.sobelButton);
        sobelButton.setOnCheckedChangeListener((compoundButton, b) -> toggleEdgeDetectionMethod());
        toggleEdgeDetectionMethod();
    }

    private void toggleEdgeDetectionMethod() {
        if (mEdgeDetectionMethod == EdgeDetectionMethod.Canny || mEdgeDetectionMethod == null) {
            mEdgeDetectionMethod = EdgeDetectionMethod.Sobel;
        } else {
            mEdgeDetectionMethod = EdgeDetectionMethod.Canny;
        }
    }

    private void initializeSegmentationLayout() {
        mThresholdingCardview = findViewById(R.id.thresholding_cardview);
        mEdgeDetectionCardView = findViewById(R.id.edge_detection_cardview);
        toggleSegmentationMethod();
        RadioButton thresholdingButton = findViewById(R.id.thresholdingButton);
        thresholdingButton.setOnCheckedChangeListener((compoundButton, b) -> toggleSegmentationMethod());
    }

    private void toggleSegmentationMethod() {
        if (mSegmentationMethod == SegmentationMethod.EDGE_DETECTION || mSegmentationMethod == null) {
            mSegmentationMethod = SegmentationMethod.THRESHOLDING;
            mEdgeDetectionCardView.setVisibility(View.GONE);
            mThresholdingCardview.setVisibility(View.VISIBLE);
        } else {
            mSegmentationMethod = SegmentationMethod.EDGE_DETECTION;
            mEdgeDetectionCardView.setVisibility(View.VISIBLE);
            mThresholdingCardview.setVisibility(View.GONE);
        }
    }

    private void initializeColorButton() {
        toggleColorSpace();
        RadioButton colorButton = findViewById(R.id.colorButton);
        colorButton.setChecked(true);
        colorButton.setOnCheckedChangeListener((compoundButton, b) -> toggleColorSpace());
    }

    private void initializeButtons() {
        mToolbar = findViewById(R.id.mainTopBar);
        mToolbar.setOnMenuItemClickListener(item -> {
            openGallery();
            return true;
        });

        Button cameraButton = findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(view -> openCamera());
    }

    private void openCamera() {
        if (mFilterMode == null || mColorSpace == null) {
            Toast.makeText(this, "Color space and filter mode must be specified!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
        intent.putExtra("kernelSize", mKernelSize);
        intent.putExtra("filterMode", mFilterMode.getValue());
        intent.putExtra("colorSpace", mColorSpace.getValue());
        intent.putExtra("hue", mHue);
        intent.putExtra("hueRadius", mHueRadius);
        intent.putExtra("saturation", mSaturation);
        intent.putExtra("saturationRadius", mSaturationRadius);
        intent.putExtra("value", mValue);
        intent.putExtra("valueRadius", mValueRadius);
        intent.putExtra("grayValue", mGrayValue);
        intent.putExtra("grayValueRadius", mGrayValueRadius);
        intent.putExtra("segmentationMethod", mSegmentationMethod.getValue());
        intent.putExtra("edgeDetectionMethod", mEdgeDetectionMethod.getValue());
        startActivity(intent);
    }

    private void openGallery() {
        Intent intent = new Intent(this, GalleryActivity.class);
        startActivity(intent);
    }

    private void initializeFilterModeCardView() {
        mFilterSizeSelectorLayout = findViewById(R.id.sizeSelectorLayout);
        mFilterSizeSelectorLayout.setVisibility(View.GONE);
        mFilterDescriptionTextView = findViewById(R.id.filterDescriptionText);

        AutoCompleteTextView filterModeSpinner = findViewById(R.id.filterModeSpinner);
        final FilterMode[] filterModes = FilterMode.values();
        ArrayAdapter<FilterMode> modeAdapter = new ArrayAdapter<>(this, R.layout.list_item, filterModes);
        filterModeSpinner.setAdapter(modeAdapter);
        filterModeSpinner.setOnItemClickListener((adapterView, view, i, l) -> setFilterMode(filterModes[i], i));
        filterModeSpinner.setText(FilterMode.None.name(), false);
        setFilterMode(filterModes[0], 0); // None
    }

    private void initializeKernelSizeSpinner() {
        AutoCompleteTextView kernelSizeSpinner = findViewById(R.id.kernelSizeSpinner);

        ArrayAdapter<Integer> sizeAdapter = new ArrayAdapter<>(this, R.layout.list_item, KERNEL_SIZES);
        kernelSizeSpinner.setAdapter(sizeAdapter);
        kernelSizeSpinner.setOnItemClickListener((adapterView, view, i, l) -> mKernelSize = KERNEL_SIZES.get(i));
        kernelSizeSpinner.setText("3", false);
        mKernelSize = 3;
    }

    private void initializeGradientView() {
        mGradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {normalizeGray(0), normalizeGray(255)});

        View gradientView = findViewById(R.id.gradientView);
        gradientView.setBackground(mGradientDrawable);
    }

    private void initializeGraySliders() {
        mGraySlidersLayout = findViewById(R.id.graySlidersLayout);

        Slider grayValueSlider = findViewById(R.id.grayValueSlider);
        grayValueSlider.addOnChangeListener((slider, value, fromUser) -> {
            mGrayValue = value;
            setGradientColor();
        });

        Slider grayValueRadiusSlider = findViewById(R.id.grayValueRadiusSlider);
        grayValueRadiusSlider.addOnChangeListener((slider, value, fromUser) -> {
            mGrayValueRadius = value;
            setGradientColor();
        });
    }

    private void initializeHsvSliders() {
        mHsvSlidersLayout = findViewById(R.id.hsvSlidersLayout);

        Slider hueSlider = findViewById(R.id.hueSlider);
        hueSlider.addOnChangeListener((slider, value, fromUser) -> {
            mHue = value;
            setGradientColor();
        });

        Slider hueRadiusSlider = findViewById(R.id.hueRadiusSlider);
        hueRadiusSlider.addOnChangeListener((slider, value, fromUser) -> {
            mHueRadius = value;
            setGradientColor();
        });

        Slider saturationSlider = findViewById(R.id.saturationSlider);
        saturationSlider.addOnChangeListener((slider, value, fromUser) -> {
            mSaturation = value;
            setGradientColor();
        });

        Slider saturationRadiusSlider = findViewById(R.id.saturationRadiusSlider);
        saturationRadiusSlider.addOnChangeListener((slider, value, fromUser) -> {
            mSaturationRadius = value;
            setGradientColor();
        });

        Slider valueSlider = findViewById(R.id.valueSlider);
        valueSlider.addOnChangeListener((slider, value, fromUser) -> {
            mValue = value;
            setGradientColor();
        });

        Slider valueRadiusSlider = findViewById(R.id.valueRadiusSlider);
        valueRadiusSlider.addOnChangeListener((slider, value, fromUser) -> {
            mValueRadius = value;
            setGradientColor();
        });
    }

    private void setGradientColor() {
        int low, hi;
        if (mColorSpace == ColorSpace.COLOR) {
            low = normalizeColor(mHue - mHueRadius, mSaturation - mSaturationRadius, mValue - mValueRadius);
            hi = normalizeColor(mHue + mHueRadius, mSaturation + mSaturationRadius, mValue + mValueRadius);
        } else {
            low = normalizeGray(mGrayValue - mGrayValueRadius);
            hi = normalizeGray(mGrayValue + mGrayValueRadius);
        }
        mGradientDrawable.setColors(new int[] {low, hi});
    }

    private int normalizeColor(float hue, float saturation, float value) {
        hue = normalizeInRangeFromZero(hue, 179);
        saturation = normalizeInRangeFromZero(saturation, 255);
        value = normalizeInRangeFromZero(value, 255);
        float[] hsv = new float[] {hue * 2, saturation, value};
        return Color.HSVToColor(hsv);
    }

    private int normalizeGray(float value) {
        value = normalizeInRangeFromZero(value, 255);
        return Color.rgb((int) value, (int) value, (int) value);
    }

    private float normalizeInRangeFromZero(float value, float range) {
        if (value < 0) {
            return 0;
        } else if (value > range) {
            return range;
        }
        return value;
    }

    private void toggleColorSpace() {
        if (mColorSpace == ColorSpace.GRAYSCALE || mColorSpace == null) {
            mColorSpace = ColorSpace.COLOR;
            mHsvSlidersLayout.setVisibility(View.VISIBLE);
            mGraySlidersLayout.setVisibility(View.GONE);
        } else {
            mColorSpace = ColorSpace.GRAYSCALE;
            mHsvSlidersLayout.setVisibility(View.GONE);
            mGraySlidersLayout.setVisibility(View.VISIBLE);
        }
    }

    private void setFilterMode(FilterMode mode, int index) {
        mFilterMode = mode;
        if (mFilterMode == FilterMode.None) {
            mFilterDescriptionTextView.setVisibility(View.GONE);
            mFilterSizeSelectorLayout.setVisibility(View.GONE);
        } else {
            mFilterDescriptionTextView.setText(KERNEL_TYPES[index - 1]);
            mFilterDescriptionTextView.setVisibility(View.VISIBLE);
            mFilterSizeSelectorLayout.setVisibility(View.VISIBLE);
        }
    }
}