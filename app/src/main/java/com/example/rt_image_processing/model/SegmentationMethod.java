package com.example.rt_image_processing.model;

import lombok.Getter;

public enum SegmentationMethod {
    THRESHOLDING(0), EDGE_DETECTION(1);

    @Getter
    private final int value;

    SegmentationMethod(int value) {
        this.value = value;
    }

    public static SegmentationMethod of(int value) {
        for (SegmentationMethod method : values()) {
            if (method.value == value) {
                return method;
            }
        }
        throw new IllegalStateException("No SegmentationMethod with integer value " + value + " exists.");
    }
}
