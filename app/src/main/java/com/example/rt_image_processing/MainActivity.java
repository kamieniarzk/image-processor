package com.example.rt_image_processing;



import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import lombok.Getter;

public class MainActivity extends Activity {
    private static final List<Integer> KERNEL_SIZES = List.of(3, 5, 7, 9);
    private static final int[] KERNEL_TYPES = new int[]{R.string.averaging, R.string.gaussian, R.string.median};

    private Button cameraButton;
    private ImageButton galleryButton;
    private Spinner filterModeSpinner;
    private Spinner filterSizeSpinner;
    private TextView mFilterDescriptionTextView;
    private int mKernelSize;

    @Getter
    private FilterMode mFilterMode;
    @Getter
    private int mFilterSize;

    public enum FilterMode {
        NONE, AVERAGING, GAUSSIAN, MEDIAN
    }

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
        galleryButton = findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(view -> openGallery());

        cameraButton = findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(view -> openCamera());
    }

    private void initializeFilterMode() {
        mFilterDescriptionTextView = findViewById(R.id.filterDescriptionText);

        filterModeSpinner = findViewById(R.id.filterModeSpinner);
        final FilterMode[] filterModes = FilterMode.values();
        ArrayAdapter<FilterMode> modeAdapter = new ArrayAdapter<>(this, R.layout.list_item, filterModes);
        filterModeSpinner.setAdapter(modeAdapter);
        filterModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mFilterMode = filterModes[i];
                if (mFilterMode == FilterMode.NONE) {
                    mFilterDescriptionTextView.setVisibility(View.GONE);
                    filterSizeSpinner.setVisibility(View.GONE);
                } else {
                    mFilterDescriptionTextView.setText(KERNEL_TYPES[i-1]);
                    mFilterDescriptionTextView.setVisibility(View.VISIBLE);
                    filterSizeSpinner.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        filterSizeSpinner = findViewById(R.id.filterSizeSpinner);
        ArrayAdapter<Integer> sizeAdapter = new ArrayAdapter<>(this, R.layout.list_item, KERNEL_SIZES);
        filterSizeSpinner.setAdapter(sizeAdapter);
        filterSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mKernelSize = KERNEL_SIZES.get(i);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void openCamera() {
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra("kernelSize", mKernelSize);
        intent.putExtra("kernelMode", mFilterMode);
        startActivity(intent);
    }

    private void openGallery() {
        Intent intent = new Intent(this, GalleryActivity.class);
        startActivity(intent);
    }
}