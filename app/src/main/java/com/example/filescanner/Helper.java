package com.example.filescanner;

import java.util.List;

public class Helper {
    public static int getIndexOfLargest(List<Double> list) {
        int maxAt = 0;
        for (int i = 0; i < list.size(); i++) {
            maxAt = list.get(i) > list.get(maxAt) ? i : maxAt;
        }
        return maxAt;
    }

    public static int getIndexOfSmallest(List<Double> list) {
        int minAt = 0;
        for (int i = 0; i < list.size(); i++) {
            minAt = list.get(i) < list.get(minAt) ? i : minAt;
        }
        return minAt;
    }
}
