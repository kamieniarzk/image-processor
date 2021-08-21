package com.imageprocessor;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initializeButtons();
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