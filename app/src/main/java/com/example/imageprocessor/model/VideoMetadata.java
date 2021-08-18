package com.example.imageprocessor.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoMetadata {
    private SegmentationMethod segmentationMethod;
    private FilteringMethod filteringMethod;
    private EdgeDetectionMethod edgeDetectionMethod;
    private MarkingMethod markingMethod;
    private ColorSpace colorSpace;
    private long segmentationTime;
    private long extractionTime;
    private long filteringTime;
}
