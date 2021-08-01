package com.example.rt_image_processing.util;

import android.content.Context;

import androidx.recyclerview.widget.GridLayoutManager;

public class GridLayoutManagerWrapper extends GridLayoutManager {
    public GridLayoutManagerWrapper(Context context, int spanCount) {
        super(context, spanCount);
    }

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return false;
    }
}
