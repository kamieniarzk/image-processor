package com.imageprocessor.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.imageprocessor.processor.params.EdgeDetectionParams;
import com.imageprocessor.processor.params.FilteringParams;
import com.imageprocessor.processor.params.MarkingParams;
import com.imageprocessor.processor.params.ThresholdingParams;
import com.imageprocessor.processor.TimingMetrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoMetadata {
    private FilteringParams filteringParams;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private EdgeDetectionParams edgeDetectionParams;
    private MarkingParams markingParams;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ThresholdingParams thresholdingParams;
    private SegmentationMethod segmentationMethod;
    private ColorSpace colorSpace;
    private TimingMetrics timingMetrics;
}
