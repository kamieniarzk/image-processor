package com.imageprocessor.model;

import lombok.Getter;

public enum FilteringMethod implements ProcessingMethod {
    None(0), Averaging(1), Gaussian(2), Median(3);

    @Getter
    private final int value;

    FilteringMethod(int value) {
        this.value = value;
    }

    public static FilteringMethod of(int value) {
        for (FilteringMethod filteringMethod : values()) {
            if (filteringMethod.value == value) {
                return filteringMethod;
            }
        }
        throw new IllegalStateException("No FilterMode with integer value " + value + " exists.");
    }
}
