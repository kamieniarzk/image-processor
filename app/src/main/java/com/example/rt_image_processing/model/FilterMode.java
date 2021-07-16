package com.example.rt_image_processing.model;

import lombok.Getter;

public enum FilterMode {
    None(0), Averaging(1), Gaussian(2), Median(3);

    @Getter
    private final int value;

    FilterMode(int value) {
        this.value = value;
    }

    public static FilterMode of(int value) {
        for (FilterMode filterMode : values()) {
            if (filterMode.value == value) {
                return filterMode;
            }
        }
        throw new IllegalStateException("No FilterMode with integer value " + value + " exists.");
    }
}
