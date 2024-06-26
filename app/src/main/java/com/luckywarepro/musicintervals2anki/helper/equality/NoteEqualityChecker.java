package com.luckywarepro.musicintervals2anki.helper.equality;

import com.luckywarepro.musicintervals2anki.helper.AnkiDroidHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public abstract class NoteEqualityChecker implements EqualityChecker {
    protected String[] modelFields;

    public NoteEqualityChecker(String[] modelFields) {
        this.modelFields = modelFields;
    }

    public String[] getModelFields() {
        return modelFields;
    }

    public void setModelFields(String[] modelFields) {
        this.modelFields = modelFields;
    }

    public static boolean areDifferent(Map<String, String> data1, Map<String, String> data2, final String key,
                                       Map<String, EqualityChecker> equalityCheckers, Map<String, String> defaultValues) {
        String value1 = Objects.requireNonNull(data1.getOrDefault(key, ""));
        String value2 = Objects.requireNonNull(data2.getOrDefault(key, ""));

        EqualityChecker defaultEqualityChecker = new FieldEqualityChecker(key, AnkiDroidHelper.DEFAULT_EQUALITY_CHECKER);
        EqualityChecker equalityChecker = Objects.requireNonNull(equalityCheckers.getOrDefault(key, defaultEqualityChecker));

        final String defaultValue = Objects.requireNonNull(defaultValues.getOrDefault(key, ""));
        Map<String, String> defaultData1 = new HashMap<String, String>(data1) {{
            put(key, defaultValue);
        }};
        Map<String, String> defaultData2 = new HashMap<String, String>(data2) {{
            put(key, defaultValue);
        }};
        boolean defaultEquality = !defaultValue.isEmpty() &&
                ((equalityChecker.areEqual(data1, defaultData2) && value2.isEmpty() ||
                        value1.isEmpty() && equalityChecker.areEqual(data2, defaultData1)) ||
                        (value1.isEmpty() && value2.isEmpty()));

        return !defaultEquality && !equalityChecker.areEqual(data1, data2);
    }
}
