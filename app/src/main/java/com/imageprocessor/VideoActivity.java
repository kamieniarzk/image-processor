package com.imageprocessor;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.chart.common.listener.Event;
import com.anychart.chart.common.listener.ListenersInterface;
import com.anychart.charts.Cartesian;
import com.anychart.charts.Pie;
import com.anychart.core.cartesian.series.Column;
import com.anychart.core.ui.ChartCredits;
import com.anychart.enums.Align;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.LegendLayout;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;
import com.bumptech.glide.Glide;
import com.imageprocessor.model.MarkingMethod;
import com.imageprocessor.model.FilteringMethod;
import com.imageprocessor.model.SegmentationMethod;
import com.imageprocessor.model.VideoMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VideoActivity extends AppCompatActivity {
    private Uri mVideoUri;
    private VideoMetadata mVideoMetadata;

    private AnyChartView mChartView;
    private Cartesian mCartesian;
    private Pie mPie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        readMetadata();
        initializeView();
        initializeConfigDetails();
        initializeChart1();
//        initializeChart2();
    }

    private void initializeView() {
        MaterialToolbar topBar = findViewById(R.id.recordingDetailsTopBar);
        topBar.setNavigationOnClickListener(view -> finish());
        topBar.setOnMenuItemClickListener(item -> {
            deleteVideo();
            return true;
        });
        ImageView videoImageView = findViewById(R.id.videoThumbnail);
        videoImageView.setOnClickListener(view -> startVideo());
        try {
            Glide.with(this).load(mVideoUri).into(videoImageView);
        } catch (Exception e) {
        }
    }

    private void deleteVideo() {
        File file = new File(mVideoUri.getPath());
        if (file.exists()) {
            if (file.delete()) {
                finish();
            } else {
                Toast.makeText(this, "Could not delete video", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void startVideo() {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("videoUri", mVideoUri);
        startActivity(intent);
    }

    private void readMetadata() {
        mVideoUri = getIntent().getParcelableExtra("videoUri");
        String lastPathSegment = mVideoUri.getLastPathSegment();
        String mediaPath;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            mediaPath = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath();
        } else {
            mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath();
        }
        String fileName = lastPathSegment.substring(0, lastPathSegment.length() - 4);
        String jsonFilePath = mediaPath + File.separator + fileName + ".json";

        ObjectMapper mapper = new ObjectMapper();
        try {
            mVideoMetadata = mapper.readValue(new File(jsonFilePath), VideoMetadata.class);
        } catch (IOException e) {
            Toast.makeText(this, "Could not load metadata for video.", Toast.LENGTH_LONG).show();
        }
    }

    private void initializeChart1() {
        AnyChartView anyChartView = findViewById(R.id.any_chart_view);
//        anyChartView.setProgressBar(findViewById(R.id.progress_bar));
        APIlib.getInstance().setActiveAnyChartView(anyChartView);
//        ChartCredits.instantiate().enabled(false);

        Cartesian cartesian = AnyChart.column();

        List<DataEntry> data = new ArrayList<>();

        data.add(new ValueDataEntry("Segmentation", mVideoMetadata.getTimingMetrics().getSegmentationTime()));

        if (mVideoMetadata.getFilteringParams().getFilteringMethod() != FilteringMethod.None) {
            data.add(new ValueDataEntry("Filtering", mVideoMetadata.getTimingMetrics().getFilteringTime()));
        }

        data.add(new ValueDataEntry("Marking", mVideoMetadata.getTimingMetrics().getMarkingTime()));

        Column column = cartesian.column(data);
        column.color("green");

        column.tooltip()
                .titleFormat("{%X}")
                .position(Position.CENTER_BOTTOM)
                .anchor(Anchor.CENTER_BOTTOM)
                .offsetX(0d)
                .offsetY(5d)
                .format("{%Value}{groupsSeparator: }ms");

        cartesian.animation(true);
        cartesian.title("CPU time consumed by each algorithm step.");

        cartesian.yScale().minimum(0d);

        cartesian.yAxis(0).labels().format("{%Value}{groupsSeparator: }ms");

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.interactivity().hoverMode(HoverMode.BY_X);

        cartesian.xAxis(0).title("Algorithm step");
        cartesian.yAxis(0).title("Time");
        mCartesian = cartesian;
        anyChartView.setChart(mCartesian);
    }

    private void initializeChart2() {
        AnyChartView anyChartView = findViewById(R.id.any_chart_view);
//        anyChartView.setProgressBar(findViewById(R.id.progress_bar1));
        APIlib.getInstance().setActiveAnyChartView(anyChartView);

        Pie pie = AnyChart.pie();

        pie.setOnClickListener(new ListenersInterface.OnClickListener(new String[]{"x", "value"}) {
            @Override
            public void onClick(Event event) {
                Toast.makeText(VideoActivity.this, event.getData().get("x") + ":" + event.getData().get("value"), Toast.LENGTH_SHORT).show();
            }
        });

        List<DataEntry> data = new ArrayList<>();
        data.add(new ValueDataEntry("Filtering", mVideoMetadata.getTimingMetrics().getFilteringTime()));
        data.add(new ValueDataEntry("Segmentation", mVideoMetadata.getTimingMetrics().getSegmentationTime()));
        data.add(new ValueDataEntry("Marking", mVideoMetadata.getTimingMetrics().getMarkingTime()));

        pie.data(data);

        pie.labels().position("outside");

        pie.legend().title().enabled(true);
        pie.legend().title()
                .text("Processing time distribution")
                .padding(0d, 0d, 10d, 0d);

        pie.legend()
                .position("center-bottom")
                .itemsLayout(LegendLayout.HORIZONTAL)
                .align(Align.CENTER);

        mPie = pie;
        anyChartView.setChart(mPie);
    }

    private void initializeConfigDetails() {
        TextView colorSpaceValue = findViewById(R.id.colorSpaceValue);
        colorSpaceValue.setText(mVideoMetadata.getColorSpace().name());

        TextView segmentationMethodValue = findViewById(R.id.segmentationMethodValue);
        segmentationMethodValue.setText(mVideoMetadata.getSegmentationMethod().name());

        TextView extractionMethodValue = findViewById(R.id.extractionMethodValue);
        extractionMethodValue.setText(mVideoMetadata.getMarkingParams().getMarkingMethod().name());

        TextView filteringMethodValue = findViewById(R.id.filteringMethodValue);
        filteringMethodValue.setText(mVideoMetadata.getFilteringParams().getFilteringMethod().name());
    }
}