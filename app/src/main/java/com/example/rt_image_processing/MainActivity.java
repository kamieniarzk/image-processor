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
import com.google.android.material.slider.Slider;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final List<Integer> KERNEL_SIZES = List.of(3, 5, 7, 9);
    private static final int[] KERNEL_TYPES = new int[]{R.string.averaging, R.string.gaussian, R.string.median};

    private TextView mFilterDescriptionTextView;
    private LinearLayout mFilterSizeSelectorLayout;
    private int mKernelSize;
    private ColorSpace mColorSpace;
    private FilterMode mFilterMode;
    private Slider mHueSlider;
    private Slider mRadiusSlider;
    private int mThresholdValue;
    private View mHueGradientView;
    private GradientDrawable mGradientDrawable;
    private int mThresholdingRadius;

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
        initializeThresholdSlider();
        initializeRadiusSlider();
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
        intent.putExtra("hueValue", mThresholdValue);
        intent.putExtra("hueRadius", mThresholdingRadius);
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
            Toast.makeText(MainActivity.this, "colorSpace = " + mColorSpace.name(), Toast.LENGTH_SHORT).show();
        });
    }

    private void initializeGradientView() {
        mGradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {normalizeColor(0), convertColorSpace(255)});

        mHueGradientView = findViewById(R.id.hueGradientView);
        mHueGradientView.setBackground(mGradientDrawable);
    }

    private void initializeThresholdSlider() {
        mHueSlider = findViewById(R.id.thresholdSlider);
        mHueSlider.addOnChangeListener((slider, value, fromUser) -> {
            mThresholdValue = (int) value;
            setGradientColor(value);
        });

        mHueSlider.setValueFrom(0);

        if (mColorSpace == ColorSpace.COLOR) {
            mHueSlider.setValueTo(255);
        } else {
            mHueSlider.setValueTo(179);
        }
    }

    private void initializeRadiusSlider() {
        mRadiusSlider = findViewById(R.id.radiusSlider);
        mRadiusSlider.addOnChangeListener((slider, value, fromUser) -> {
            mThresholdingRadius = (int) value;
            setGradientColor(value);
        });
    }

    private int convertColorSpace(float mHueValue) {
        if (mHueValue < 0) {
            mHueValue = 180 - mHueValue;
        } else if (mHueValue >= 180) {
            mHueValue = 180 - mHueValue;
        }
        float[] hsv = new float[] {mHueValue * 2, 100, 100};
        return Color.HSVToColor(hsv);
    }

    private void setGradientColor(float value) {
        mGradientDrawable.setColors(new int[] {normalizeColor(value - mThresholdingRadius), normalizeColor(value + mThresholdingRadius)});
    }

    private int normalizeColor(float value) {
        if (mColorSpace == ColorSpace.COLOR) {
            if (value < 0) {
                value = 180 - value;
            } else if (value > 179) {
                value = 180 - value;
            }
            float[] hsv = new float[] {value * 2, 100, 100};
            return Color.HSVToColor(hsv);
        } else {
            if (value < 0) {
                value = 0;
            } else if (value > 255) {
                value =  255;
            }
            return Color.rgb((int) value, (int) value, (int) value);
        }
    }
}