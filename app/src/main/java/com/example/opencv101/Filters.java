package com.example.opencv101;

public enum Filters {
    SOBEL_X(new int[][]{
            {1,0,-1},
            {2,0,-2},
            {1,0,-1}
    }),
    SOBEL_Y(new int[][]{
            {1,2,1},
            {0,0,0},
            {-1,-2,-1}
    });

    public int[][] filter;

    Filters(int[][] filter) {
        this.filter = filter;
    }
}
