package com.example.rt_image_processing;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rt_image_processing.model.ColorSpace;
import com.example.rt_image_processing.model.FilterMode;
import com.example.rt_image_processing.model.SegmentationMethod;
import com.google.android.material.slider.Slider;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final List<Integer> KERNEL_SIZES = List.of(3, 5, 7, 9);
    private static final int[] KERNEL_TYPES = new int[]{R.string.averaging, R.string.gaussian, R.string.median};

    private TextView mFilterDescriptionTextView;
    private LinearLayout mFilterSizeSelectorLayout;
    private LinearLayout mHsvSlidersLayout;
    private LinearLayout mGraySlidersLayout;
    private LinearLayout mThresholdingLayout;
    private LinearLayout mEdgeDetectionLayout;
    private int mKernelSize;
    private ColorSpace mColorSpace;
    private FilterMode mFilterMode;
    private SegmentationMethod mSegmentationMethod;
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
    }

    private void initializeSegmentationLayout() {
        mThresholdingLayout = findViewById(R.id.thresholdingLayout);
        mEdgeDetectionLayout = findViewById(R.id.edgeDetectionLayout);
        toggleSegmentationMethod();

        RadioButton thresholdingButton = findViewById(R.id.thresholdingButton);
        thresholdingButton.setOnCheckedChangeListener((compoundButton, b) -> toggleSegmentationMethod());
    }

    private void toggleSegmentationMethod() {
        if (mSegmentationMethod == SegmentationMethod.EDGE_DETECTION || mSegmentationMethod == null) {
            mSegmentationMethod = SegmentationMethod.EDGE_DETECTION;
            mEdgeDetectionLayout.setVisibility(View.VISIBLE);
            mThresholdingLayout.setVisibility(View.GONE);
        } else {
            mSegmentationMethod = SegmentationMethod.THRESHOLDING;
            mEdgeDetectionLayout.setVisibility(View.GONE);
            mThresholdingLayout.setVisibility(View.VISIBLE);
        }
    }

    private void initializeColorButton() {
        toggleColorSpace();
        RadioButton colorButton = findViewById(R.id.colorButton);
        colorButton.setChecked(true);
        colorButton.setOnCheckedChangeListener((compoundButton, b) -> toggleColorSpace());
    }

    private void initializeButtons() {
        ImageButton galleryButton = findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(view -> openGallery());

        Button cameraButton = findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(view -> openCamera());
    }

    private void openCamera() {
        if (mFilterMode == null || mColorSpace == null) {
            Toast.makeText(this, "Color space and filter mode must be specified!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getApplicationContext(), CamActivity.class);
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
        filterModeSpinner.setOnItemClickListener((adapterView, view, i, l) -> {
            mFilterMode = filterModes[i];
            if (mFilterMode == FilterMode.NONE) {
                mFilterDescriptionTextView.setVisibility(View.GONE);
                mFilterSizeSelectorLayout.setVisibility(View.GONE);
            } else {
                mFilterDescriptionTextView.setText(KERNEL_TYPES[i-1]);
                mFilterDescriptionTextView.setVisibility(View.VISIBLE);
                mFilterSizeSelectorLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initializeKernelSizeSpinner() {
        AutoCompleteTextView kernelSizeSpinner = findViewById(R.id.kernelSizeSpinner);

        ArrayAdapter<Integer> sizeAdapter = new ArrayAdapter<>(this, R.layout.list_item, KERNEL_SIZES);
        kernelSizeSpinner.setAdapter(sizeAdapter);
        kernelSizeSpinner.setOnItemClickListener((adapterView, view, i, l) -> mKernelSize = KERNEL_SIZES.get(i));
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
        if (mColorSpace == ColorSpace.GRAY || mColorSpace == null) {
            mColorSpace = ColorSpace.COLOR;
            mHsvSlidersLayout.setVisibility(View.VISIBLE);
            mGraySlidersLayout.setVisibility(View.GONE);
        } else {
            mColorSpace = ColorSpace.GRAY;
            mHsvSlidersLayout.setVisibility(View.GONE);
            mGraySlidersLayout.setVisibility(View.VISIBLE);
        }
    }
}