package com.example.rt_image_processing.model;

import lombok.Getter;

public enum FilterMode {
    NONE(0), AVERAGING(1), GAUSSIAN(2), MEDIAN(3);

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
