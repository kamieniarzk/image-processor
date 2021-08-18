package com.example.imageprocessor.model;

import lombok.Getter;

public enum MarkingMethod implements ProcessingMethod {
    BACKGROUND_CHANGE(0), DRAW_CONTOURS(1);

    @Getter
    private final int value;

    MarkingMethod(int value) {
        this.value = value;
    }

    public static MarkingMethod of(int value) {
        for (MarkingMethod method : values()) {
            if (method.value == value) {
                return method;
            }
        }
        throw new IllegalStateException("No MarkingMethod with integer value " + value + " exists.");
    }
}