package com.luckywarepro.musicintervals2anki.helper.equality;

import java.util.Map;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class FieldEqualityChecker implements EqualityChecker {
    private String modelField;
    private final ValueEqualityChecker valueEqualityChecker;

    public FieldEqualityChecker(String modelField, ValueEqualityChecker valueEqualityChecker) {
        this.modelField = modelField;
        this.valueEqualityChecker = valueEqualityChecker;
    }

    @Override
    public boolean areEqual(Map<String, String> data1, Map<String, String> data2) {
        String value1 = data1.getOrDefault(modelField, "");
        String value2 = data2.getOrDefault(modelField, "");
        return valueEqualityChecker.areEqual(value1, value2);
    }

    public void setField(String modelField) {
        this.modelField = modelField;
    }
}
