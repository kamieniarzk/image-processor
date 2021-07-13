package com.example.rt_image_processing;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rt_image_processing.model.ColorSpace;
import com.example.rt_image_processing.model.FilterMode;
import com.example.rt_image_processing.processor.ImageProcessor;
import com.google.android.material.slider.Slider;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final List<Integer> KERNEL_SIZES = List.of(3, 5, 7, 9);
    private static final int[] KERNEL_TYPES = new int[]{R.string.averaging, R.string.gaussian, R.string.median};


    private TextView mFilterDescriptionTextView;
    private LinearLayout mFilterSizeSelectorLayout;
    private LinearLayout mHsvSlidersLayout;
    private LinearLayout mGraySlidersLayout;
    private int mKernelSize;
    private ColorSpace mColorSpace;
    private FilterMode mFilterMode;
    private float mGrayValue;
    private float mGrayValueRadius;
    private float mHue;
    private float mSaturation;
    private float mValue;
    private float mHueRadius;
    private float mValueRadius;
    private float mSaturationRadius;
    private float mMinContourArea = 0.1f;
    private GradientDrawable mGradientDrawable;

    public MainActivity() {

    }

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
        initializeColorSpaceSpinner();
        initializeGradientView();
        initializeHsvSliders();
        initializeGraySliders();
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
        Toast.makeText(MainActivity.this, "put filterMode as " + mFilterMode.getValue(), Toast.LENGTH_LONG);
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

    private void initializeColorSpaceSpinner() {
        AutoCompleteTextView colorSpaceSpinner = findViewById(R.id.colorSpaceSpinner);
        colorSpaceSpinner.setInputType(InputType.TYPE_NULL);

        ColorSpace[] colorSpaces = ColorSpace.values();

        ArrayAdapter<ColorSpace> colorSpaceAdapter = new ArrayAdapter<>(this, R.layout.list_item, colorSpaces);
        colorSpaceSpinner.setAdapter(colorSpaceAdapter);
        colorSpaceSpinner.setOnItemClickListener((adapterView, view, i, l) -> {
            mColorSpace = colorSpaces[i];
            if (mColorSpace == ColorSpace.COLOR) {
                mHsvSlidersLayout.setVisibility(View.VISIBLE);
                mGraySlidersLayout.setVisibility(View.GONE);
            } else {
                mHsvSlidersLayout.setVisibility(View.GONE);
                mGraySlidersLayout.setVisibility(View.VISIBLE);
            }
        });
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
        hue = normalizeInRange(hue, 0, 179);
        saturation = normalizeInRange(saturation, 0, 255);
        value = normalizeInRange(value, 0, 255);
        float[] hsv = new float[] {hue * 2, saturation, value};
        return Color.HSVToColor(hsv);
    }

    private int normalizeGray(float value) {
        value = normalizeInRange(value, 0, 255);
        return Color.rgb((int) value, (int) value, (int) value);
    }

    private float normalizeInRange(float value, float low, float hi) {
        if (value < low) {
            return low;
        } else if (value > hi) {
            return hi;
        }
        return value;
    }
}