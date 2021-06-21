package com.example.rt_image_processing;



import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;

import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends Activity {
    private Button cameraButton;
    private ImageButton galleryButton;


    public MainActivity() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeButtons();
    }

    private void initializeButtons() {
        galleryButton = findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(view -> openGallery());

        cameraButton = findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(view -> openCamera());
    }

    private void openCamera() {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    private void openGallery() {
        Intent intent = new Intent(this, GalleryActivity.class);
        startActivity(intent);
    }
}