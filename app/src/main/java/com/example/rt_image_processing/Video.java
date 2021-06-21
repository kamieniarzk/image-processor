package com.example.rt_image_processing;

import android.net.Uri;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Video {
    private long id;
    private Uri data;
}
