package com.example.rt_image_processing;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initializeTopBar();
        initializeButtons();
    }

    private void initializeTopBar() {
//        MaterialToolbar topBar = findViewById(R.id.homeTopBar);
//        topBar.setOnMenuItemClickListener(item -> {
//            if (item.getItemId() == R.id.config) {
//                startConfigActivity();
//            } else {
//                startGalleryActivity();
//            }
//            return true;
//        });
    }

    private void initializeButtons() {
        LinearLayout galleryLayout = findViewById(R.id.mediaGalleryButton);
        galleryLayout.setOnClickListener(view -> startGalleryActivity());
        LinearLayout configLayout = findViewById(R.id.algorithmConfigButton);
        configLayout.setOnClickListener(view -> startConfigActivity());
    }

    private void startGalleryActivity() {
        Intent intent = new Intent(this, GalleryActivity.class);
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    private void startConfigActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }
}