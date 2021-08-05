package com.luckywarepro.musicintervals2anki.model;

import java.util.Map;
import java.util.Objects;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class LargestValueComparator extends RelativesPriorityComparator {
    public LargestValueComparator(String fieldKey) {
        super(fieldKey);
    }

    @Override
    public int compare(Map<String, String> stringStringMap, Map<String, String> t1) {
        String modelField = modelFields.getOrDefault(fieldKey, fieldKey);
        String v1 = Objects.requireNonNull(stringStringMap.getOrDefault(modelField, ""));
        String v2 = Objects.requireNonNull(t1.getOrDefault(modelField, ""));
        boolean v1Empty = v1.isEmpty();
        boolean v2Empty = v2.isEmpty();
        if (v1Empty || v2Empty) {
            return v1Empty && v2Empty ? 0 :
                    v1Empty ? -1 : 1;
        }
        return Long.compare(Long.parseLong(v1), Long.parseLong(v2));
    }
}
