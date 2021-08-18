package com.example.imageprocessor.model;

import lombok.Getter;

public enum EdgeDetectionMethod implements ProcessingMethod {
    Sobel(0), Canny(1);

    @Getter
    private final int value;

    EdgeDetectionMethod(int value) {
        this.value = value;
    }

    public static EdgeDetectionMethod of(int value) {
        for (EdgeDetectionMethod method : values()) {
            if (method.value == value) {
                return method;
            }
        }
        throw new IllegalStateException("No EdgeDetectionMethod with integer value " + value + " exists.");
    }
}
