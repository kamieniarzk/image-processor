package com.example.imageprocessor;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imageprocessor.model.Video;
import com.example.imageprocessor.util.GridLayoutManagerWrapper;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.util.ArrayList;


public class GalleryActivity extends AppCompatActivity {
    private ActivityResultLauncher<String> requestPermissionLauncher;

    private ArrayList<Video> videosList = new ArrayList<>();
    private VideoAdapter adapterVideoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        initializeViews();
        registerRequestPermissionLauncher();
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
            File[] files = mediaDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getPath().endsWith(".mp4")) {
                        Uri videoUri = Uri.fromFile(file);
                        videosList.add(new Video(videoUri));
                        runOnUiThread(() -> adapterVideoList.notifyItemInserted(videosList.size() - 1));
                    }
                }
            }
        }).start();
    }

    private void registerRequestPermissionLauncher() {
        if (requestPermissionLauncher == null) {
            requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                return;
            });
        }
    }

    private void checkPermissions() {
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