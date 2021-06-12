package com.example.rt_image_processing;

import android.net.Uri;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Video {
    long id;
    Uri data;
    String title;
    String duration;
}
