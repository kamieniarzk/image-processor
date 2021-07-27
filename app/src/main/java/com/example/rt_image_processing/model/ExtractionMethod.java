package com.example.rt_image_processing.model;

import lombok.Getter;

public enum ExtractionMethod {
    BACKGROUND_CHANGE(0), DRAW_CONTOURS(1);

    @Getter
    private final int value;

    ExtractionMethod(int value) {
        this.value = value;
    }

    public static ExtractionMethod of(int value) {
        for (ExtractionMethod method : values()) {
            if (method.value == value) {
                return method;
            }
        }
        throw new IllegalStateException("No MarkingMethod with integer value " + value + " exists.");
    }
}