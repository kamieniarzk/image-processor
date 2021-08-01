package com.example.rt_image_processing;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rt_image_processing.model.Video;
import com.example.rt_image_processing.util.GridLayoutManagerWrapper;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;


public class GalleryActivity extends AppCompatActivity {
    private ActivityResultLauncher<String> requestPermissionLauncher;

    private ArrayList<Video> videosList = new ArrayList<>();
    private VideoAdapter adapterVideoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        initializeViews();
        checkPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissions();
    }

    @Override
    protected void onPause() {
        super.onPause();
        videosList.clear();
    }

    private void initializeViews() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView_videos);
        recyclerView.setLayoutManager(new GridLayoutManagerWrapper(this, 3));
        adapterVideoList = new VideoAdapter(this, videosList);
        recyclerView.setAdapter(adapterVideoList);
        MaterialToolbar topBar = findViewById(R.id.galleryTopBar);
        topBar.setNavigationOnClickListener(view -> finish());
    }

    public void initVideos() {
        new Thread(() -> {
            String mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getPath();
            File mediaDir = new File(mediaPath);
            for (File file : Objects.requireNonNull(mediaDir.listFiles())) {
                Uri videoUri = Uri.fromFile(file);
                videosList.add(new Video(videoUri));
                runOnUiThread(() -> adapterVideoList.notifyItemInserted(videosList.size() - 1));
            }
        }).start();
    }

    private void checkPermissions() {
        if (requestPermissionLauncher == null) {
            requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                return;
            });
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(
                    Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        initVideos();
    }
}