package com.example.rt_image_processing;



import android.content.Intent;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
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

import lombok.Getter;

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
    private int mHueValue;
    private View mHueGradientView;
    private GradientDrawable mGradientDrawable;
    private int mThresholdingRadius;

    public MainActivity() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeButtons();
        initializeFilterMode();
    }

    private void initializeButtons() {
        ImageButton galleryButton = findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(view -> openGallery());

        Button cameraButton = findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(view -> openCamera());
    }

    private void initializeFilterMode() {
        mFilterSizeSelectorLayout = findViewById(R.id.sizeSelectorLayout);
        mFilterSizeSelectorLayout.setVisibility(View.GONE);
        mFilterDescriptionTextView = findViewById(R.id.filterDescriptionText);

        AutoCompleteTextView filterModeSpinner = findViewById(R.id.filterModeSpinner);
        final FilterMode[] filterModes = FilterMode.values();
        ArrayAdapter<FilterMode> modeAdapter = new ArrayAdapter<>(this, R.layout.list_item, filterModes);
        filterModeSpinner.setAdapter(modeAdapter);
        filterModeSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 mFilterMode = filterModes[i];
                 if (mFilterMode == FilterMode.NONE) {
                     mFilterDescriptionTextView.setVisibility(View.GONE);
                     mFilterSizeSelectorLayout.setVisibility(View.GONE);
                 } else {
                     mFilterDescriptionTextView.setText(KERNEL_TYPES[i-1]);
                     mFilterDescriptionTextView.setVisibility(View.VISIBLE);
                     mFilterSizeSelectorLayout.setVisibility(View.VISIBLE);
                 }
             }
         });

        AutoCompleteTextView kernelSizeSpinner = findViewById(R.id.kernelSizeSpinner);

        ArrayAdapter<Integer> sizeAdapter = new ArrayAdapter<>(this, R.layout.list_item, KERNEL_SIZES);
        kernelSizeSpinner.setAdapter(sizeAdapter);
        kernelSizeSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mKernelSize = KERNEL_SIZES.get(i);
            }
        });

        AutoCompleteTextView colorSpaceSpinner = findViewById(R.id.colorSpaceSpinner);
        colorSpaceSpinner.setInputType(InputType.TYPE_NULL);

        ColorSpace[] colorSpaces = ColorSpace.values();

        ArrayAdapter<ColorSpace> colorSpaceAdapter = new ArrayAdapter<>(this, R.layout.list_item, colorSpaces);
        colorSpaceSpinner.setAdapter(colorSpaceAdapter);
        colorSpaceSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mColorSpace = colorSpaces[i];
                Toast.makeText(MainActivity.this, "colorSpace = " + mColorSpace.name(), Toast.LENGTH_SHORT).show();
            }
        });

        mGradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {convertScalarHsv2Rgba(-15), convertScalarHsv2Rgba(15)});

        mHueGradientView = findViewById(R.id.hueGradientView);
        mHueGradientView.setBackground(mGradientDrawable);

        mHueSlider = findViewById(R.id.hueSlider);
        mHueSlider.addOnChangeListener((slider, value, fromUser) -> {
            mHueValue = (int) value;
            mGradientDrawable.setColors(new int[] {convertScalarHsv2Rgba(mHueValue - mThresholdingRadius),
                                                   convertScalarHsv2Rgba(mHueValue + mThresholdingRadius)});
        });

        mRadiusSlider = findViewById(R.id.thresholdRadius);
        mRadiusSlider.addOnChangeListener((slider, value, fromUser) -> {
            mThresholdingRadius = (int) value;
            mGradientDrawable.setColors(new int[] {convertScalarHsv2Rgba(mHueValue - mThresholdingRadius),
                    convertScalarHsv2Rgba(mHueValue + mThresholdingRadius)});
        });
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
        intent.putExtra("hueValue", mHueValue);
        intent.putExtra("hueRadius", mThresholdingRadius);
        startActivity(intent);
        Toast.makeText(MainActivity.this, "put filterMode as " + mFilterMode.getValue(), Toast.LENGTH_LONG);
    }

    private void openGallery() {
        Intent intent = new Intent(this, GalleryActivity.class);
        startActivity(intent);
    }

    private int convertScalarHsv2Rgba(float mHueValue) {
        if (mHueValue < 0) {
            mHueValue = 180 - mHueValue;
        } else if (mHueValue >= 180) {
            mHueValue = 180 - mHueValue;
        }
        float[] hsv = new float[] {mHueValue * 2, 100, 100};
        return Color.HSVToColor(hsv);
    }
}