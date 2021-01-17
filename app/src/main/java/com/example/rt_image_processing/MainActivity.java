package com.example.rt_image_processing;



import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;


import android.view.View;

import android.widget.Button;

public class MainActivity extends Activity {
    private Button button;


    public MainActivity() {

    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               openCamera();
           }
        });
    }

    private void openCamera() {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }


}