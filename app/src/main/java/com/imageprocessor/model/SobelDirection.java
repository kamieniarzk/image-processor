package com.imageprocessor.model;

import lombok.Getter;

public enum SobelDirection {
    X(0), Y(1), XY(2);

    @Getter
    private final int value;

    SobelDirection(int value) {
        this.value = value;
    }

    public static SobelDirection of(int value) {
        for (SobelDirection direction : values()) {
            if (direction.value == value) {
                return direction;
            }
        }
        throw new IllegalStateException("No SobelDirection with integer value " + value + " exists.");
    }
}
