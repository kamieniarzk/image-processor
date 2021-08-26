package com.imageprocessor;

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
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.imageprocessor.model.ColorSpace;
import com.imageprocessor.model.EdgeDetectionMethod;
import com.imageprocessor.model.FilteringMethod;
import com.imageprocessor.model.MarkingMethod;
import com.imageprocessor.model.SegmentationMethod;
import com.imageprocessor.model.SobelDirection;
import com.imageprocessor.processor.params.EdgeDetectionParams;
import com.imageprocessor.processor.params.FilteringParams;
import com.imageprocessor.processor.params.MarkingParams;
import com.imageprocessor.processor.params.ThresholdingParams;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.slider.Slider;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final List<Integer> KERNEL_SIZES = List.of(3, 5, 7, 9);
    private static final int[] KERNEL_TYPES = new int[]{R.string.averaging, R.string.gaussian, R.string.median};

    // GUI
    private TextView mFilterDescriptionTextView;
    private LinearLayout mFilterSizeSelectorLayout;
    private LinearLayout mHsvSlidersLayout;
    private LinearLayout mGraySlidersLayout;
    private LinearLayout mColorSubsitutionLayout;
    private LinearLayout mDrawContoursLayout;
    private LinearLayout mSobelLayout;
    private LinearLayout mCannyLayout;
    private RadioGroup mMarkingMethodSelector;
    private MaterialCardView mThresholdingCardview;
    private MaterialCardView mEdgeDetectionCardView;
    private MaterialToolbar mToolbar;
    private GradientDrawable mThresholdGradient;
    private View mBackgroundColorPreview;
    private View mContourPreview;
    private TextView mMarkingMethodDescriptionTextView;

    // config
    private int mKernelSize;
    private ColorSpace mColorSpace;
    private FilteringMethod mFilteringMethod;
    private SegmentationMethod mSegmentationMethod;
    private EdgeDetectionMethod mEdgeDetectionMethod;
    private MarkingMethod mMarkingMethod;
    private int mGrayValue;
    private int mGrayValueRadius;
    private int mHue;
    private int mHueRadius;
    private float mSaturation;
    private float mValue;
    private float mValueRadius;
    private float mSaturationRadius;
    private int mBackgroundHue;
    private float mBackgroundSaturation;
    private float mBackgroundValue;
    private int mContourHue;
    private float mContourSaturation;
    private float mContourValue;
    private float mContourArea;
    private int mContourThickness;
    private double mCannyT1;
    private double mCannyT2;
    private SobelDirection mSobelDirection;


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
        initializeMarkingLayout();
        initializeSegmentationLayout();
        initializeEdgeDetectionLayout();
        initializeColorSubstitutionConfigLayout();
        initializeCannySliders();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mColorSpace == null) {
            mColorSpace = ColorSpace.COLOR;
        }

        if (mSegmentationMethod == null) {
            mSegmentationMethod = SegmentationMethod.THRESHOLDING;
        }

        if (mEdgeDetectionMethod == null) {
            mEdgeDetectionMethod = EdgeDetectionMethod.Canny;
        }

        if (mFilteringMethod == null) {
            mFilteringMethod = FilteringMethod.None;
        }

        if (mMarkingMethod == null) {
            mMarkingMethod = MarkingMethod.BACKGROUND_CHANGE;
        }
    }

    private void initializeCannySliders() {
        Slider threshold1 = findViewById(R.id.cannyThreshold1);
        threshold1.addOnChangeListener((slider, value, fromUser) -> mCannyT1 = value);

        Slider threshold2 = findViewById(R.id.cannyThreshold2);
        threshold2.addOnChangeListener((slider, value, fromUser) -> mCannyT2 = value);
    }

    private void initializeMarkingLayout() {
        mMarkingMethodDescriptionTextView = findViewById(R.id.markingMethodDescription);
        mMarkingMethodSelector = findViewById(R.id.markingMethodSelector);
        mColorSubsitutionLayout = findViewById(R.id.substitute_color_layout);
        mDrawContoursLayout = findViewById(R.id.draw_contours_layout);
        RadioButton backgroundChangeButton = findViewById(R.id.drawContoursButton);
        backgroundChangeButton.setOnCheckedChangeListener((comoundButton, b) -> toggleMarkingMethod());
        toggleMarkingMethod();
        initializeContourDrawingConfig();
    }

    private void toggleMarkingMethod() {
        if (mMarkingMethod == MarkingMethod.DRAW_CONTOURS || mMarkingMethod == null) {
            mMarkingMethod = MarkingMethod.BACKGROUND_CHANGE;
            mColorSubsitutionLayout.setVisibility(View.VISIBLE);
            mDrawContoursLayout.setVisibility(View.GONE);
            mMarkingMethodDescriptionTextView.setText(R.string.substitute_colors_desc);
        } else {
            mMarkingMethod = MarkingMethod.DRAW_CONTOURS;
            mColorSubsitutionLayout.setVisibility(View.GONE);
            mDrawContoursLayout.setVisibility(View.VISIBLE);
            mMarkingMethodDescriptionTextView.setText(R.string.draw_contours_and_label_desc);
        }
    }

    private void initializeEdgeDetectionLayout() {
        mCannyLayout = findViewById(R.id.cannyLayout);
        mSobelLayout = findViewById(R.id.sobelLayout);
        mSobelDirection = SobelDirection.X;
        RadioButton sobelButton = findViewById(R.id.sobelButton);
        sobelButton.setOnCheckedChangeListener((compoundButton, b) -> toggleEdgeDetectionMethod());
        toggleEdgeDetectionMethod();

        AutoCompleteTextView sobelDirectionSpinner = findViewById(R.id.sobelDirectionSpinner);
        final SobelDirection[] sobelDirections = SobelDirection.values();
        ArrayAdapter<SobelDirection> directionAdapter = new ArrayAdapter<>(this, R.layout.list_item, sobelDirections);
        sobelDirectionSpinner.setAdapter(directionAdapter);
        sobelDirectionSpinner.setOnItemClickListener((adapterView, view, i, l) -> mSobelDirection = sobelDirections[i]);
        mSobelDirection = SobelDirection.X;
    }

    private void toggleEdgeDetectionMethod() {
        if (mEdgeDetectionMethod == EdgeDetectionMethod.Canny || mEdgeDetectionMethod == null) {
            mEdgeDetectionMethod = EdgeDetectionMethod.Sobel;
            mCannyLayout.setVisibility(View.GONE);
            mSobelLayout.setVisibility(View.VISIBLE);
        } else {
            mEdgeDetectionMethod = EdgeDetectionMethod.Canny;
            mCannyLayout.setVisibility(View.VISIBLE);
            mSobelLayout.setVisibility(View.GONE);
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
            mMarkingMethodSelector.setVisibility(View.VISIBLE);
            mSegmentationMethod = SegmentationMethod.THRESHOLDING;
            mEdgeDetectionCardView.setVisibility(View.GONE);
            mThresholdingCardview.setVisibility(View.VISIBLE);
        } else {
            mMarkingMethodSelector.setVisibility(View.GONE);
            mMarkingMethod = MarkingMethod.DRAW_CONTOURS;
            toggleMarkingMethod();
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

        mToolbar.setNavigationOnClickListener((view) -> startHomeActivity());

        Button cameraButton = findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(view -> openCamera());
    }

    private void startHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    private void openCamera() {
        if (mFilteringMethod == null || mColorSpace == null) {
            Toast.makeText(this, "Color space and filter mode must be specified!", Toast.LENGTH_SHORT).show();
            return;
        }

        int rgbBackground = Color.HSVToColor(new float[] {2 * mBackgroundHue, mBackgroundSaturation, mBackgroundValue});
        int backgroundRed = Color.red(rgbBackground);
        int backgroundGreen = Color.green(rgbBackground);
        int backgroundBlue = Color.blue(rgbBackground);

        int rgbContour = Color.HSVToColor(new float[] {2 * mContourHue, mContourSaturation, mContourValue});
        int contourRed = Color.red(rgbContour);
        int contourGreen = Color.green(rgbContour);
        int contourBlue = Color.blue(rgbContour);

        ThresholdingParams thresholdingParams = mSegmentationMethod == SegmentationMethod.EDGE_DETECTION ?
                null :
                new ThresholdingParams(
                mHue,
                mHueRadius,
                mSaturation,
                mSaturationRadius,
                mValue,
                mValueRadius,
                mGrayValue,
                mGrayValueRadius
        );

        FilteringParams filteringParams = new FilteringParams(
                mFilteringMethod.getValue(),
                mKernelSize
        );

        MarkingParams markingParams = new MarkingParams(
                mMarkingMethod,
                backgroundRed,
                backgroundGreen,
                backgroundBlue,
                contourRed,
                contourGreen,
                contourBlue,
                mContourArea,
                mContourThickness
        );

        EdgeDetectionParams edgeDetectionParams = mSegmentationMethod == SegmentationMethod.THRESHOLDING ?
                null :
                new EdgeDetectionParams(
                mEdgeDetectionMethod,
                mCannyT1,
                mCannyT2,
                mSobelDirection
        );

        Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
        intent.putExtra("colorSpace", mColorSpace.getValue());
        intent.putExtra("filteringParams", filteringParams);
        intent.putExtra("segmentationMethod", mSegmentationMethod.getValue());
        intent.putExtra("thresholdingParams", thresholdingParams);
        intent.putExtra("edgeDetectionParams", edgeDetectionParams);
        intent.putExtra("markingParams", markingParams);
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
        final FilteringMethod[] filteringMethods = FilteringMethod.values();
        ArrayAdapter<FilteringMethod> modeAdapter = new ArrayAdapter<>(this, R.layout.list_item, filteringMethods);
        filterModeSpinner.setAdapter(modeAdapter);
        filterModeSpinner.setOnItemClickListener((adapterView, view, i, l) -> setFilterMode(filteringMethods[i], i));
        filterModeSpinner.setText(FilteringMethod.None.name(), false);
        setFilterMode(filteringMethods[0], 0); // None
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
        mThresholdGradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {normalizeGray(0), normalizeGray(255)});

        View gradientView = findViewById(R.id.gradientView);
        gradientView.setBackground(mThresholdGradient);

    }

    private void initializeGraySliders() {
        mGraySlidersLayout = findViewById(R.id.graySlidersLayout);

        Slider grayValueSlider = findViewById(R.id.grayValueSlider);
        grayValueSlider.addOnChangeListener((slider, value, fromUser) -> {
            mGrayValue = (int) value;
            setGradientColor();
        });

        Slider grayValueRadiusSlider = findViewById(R.id.grayValueRadiusSlider);
        grayValueRadiusSlider.addOnChangeListener((slider, value, fromUser) -> {
            mGrayValueRadius = (int) value;
            setGradientColor();
        });
    }

    private void initializeHsvSliders() {
        mHsvSlidersLayout = findViewById(R.id.hsvSlidersLayout);

        Slider hueSlider = findViewById(R.id.hueSlider);
        hueSlider.addOnChangeListener((slider, value, fromUser) -> {
            mHue = (int) value;
            setGradientColor();
        });

        Slider hueRadiusSlider = findViewById(R.id.hueRadiusSlider);
        hueRadiusSlider.addOnChangeListener((slider, value, fromUser) -> {
            mHueRadius = (int) value;
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

    private void initializeColorSubstitutionConfigLayout() {
        mBackgroundColorPreview = findViewById(R.id.backgroundColorView);

        Slider hueSlider = findViewById(R.id.backgroudHueSlider);
        hueSlider.addOnChangeListener((slider, value, fromUser) -> {
            mBackgroundHue = (int) value;
            setBackgroundPaneColor();
        });

        Slider saturationSlider = findViewById(R.id.backgroundSaturationSlider);
        saturationSlider.addOnChangeListener((slider, value, fromUser) -> {
            mBackgroundSaturation = value;
            setBackgroundPaneColor();
        });

        Slider valueSlider = findViewById(R.id.backgroundValueSlider);
        valueSlider.addOnChangeListener((slider, value, fromUser) -> {
            mBackgroundValue = value;
            setBackgroundPaneColor();
        });
    }

    private void initializeContourDrawingConfig() {
        mContourThickness = 10;
        mContourPreview = findViewById(R.id.contourPreview);
        mContourPreview.setBackgroundColor(normalizeColor(180, 1 , 1));

        Slider contourHueSlider = findViewById(R.id.contourHueSlider);
        contourHueSlider.addOnChangeListener((slider, value, fromUser) -> {
            mContourHue = (int) value;
            setContourPreview();
        });

        Slider contourSaturationSlider = findViewById(R.id.contourSaturationSlider);
        contourSaturationSlider.addOnChangeListener((slider, value, fromUser) -> {
            mContourSaturation = value;
            setContourPreview();
        });

        Slider contourValueSlider = findViewById(R.id.contourValueSlider);
        contourValueSlider.addOnChangeListener((slider, value, fromUser) -> {
            mContourValue = value;
            setContourPreview();
        });

        Slider contourAreaSlider = findViewById(R.id.contourAreaSlider);
        contourAreaSlider.addOnChangeListener((slider, value, fromUser) -> {
            mContourArea = value;
            setContourPreview();
        });

        Slider contourThicknessSlider = findViewById(R.id.contourThicknessSlider);
        contourThicknessSlider.addOnChangeListener((slider, value, fromUser) -> {
            mContourThickness = (int) value;
            setContourPreview();
        });
    }

    private void setContourPreview() {
        mContourPreview.getLayoutParams().height = mContourThickness;
        mContourPreview.setBackgroundColor(normalizeColor(mContourHue, mContourSaturation, mContourValue));
        mContourPreview.requestLayout();
    }

    private void setBackgroundPaneColor() {
        int color = normalizeColor(mBackgroundHue, mBackgroundSaturation, mBackgroundValue);
        mBackgroundColorPreview.getBackground().setTint(color);
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
        mThresholdGradient.setColors(new int[] {low, hi});
    }


    private int normalizeColor(float hue, float saturation, float value) {
        hue = normalizeHue(hue);
        saturation = normalizeInRangeFromZero(saturation, 1);
        value = normalizeInRangeFromZero(value, 1);
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
        setGradientColor();
    }

    private void setFilterMode(FilteringMethod mode, int index) {
        mFilteringMethod = mode;
        if (mFilteringMethod == FilteringMethod.None) {
            mFilterDescriptionTextView.setVisibility(View.GONE);
            mFilterSizeSelectorLayout.setVisibility(View.GONE);
        } else {
            mFilterDescriptionTextView.setText(KERNEL_TYPES[index - 1]);
            mFilterDescriptionTextView.setVisibility(View.VISIBLE);
            mFilterSizeSelectorLayout.setVisibility(View.VISIBLE);
        }
    }

    private int normalizeHue(float value) {
        if (value < 0) {
            return (int) (179 - value);
        } else if (value > 179) {
            return (int) (value - 179);
        }
        return (int) value;
    }
}