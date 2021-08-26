package com.imageprocessor.processor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimingMetrics {
    private double filteringTime;
    private double segmentationTime;
    private double markingTime;
}
