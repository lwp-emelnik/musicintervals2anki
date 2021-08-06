package com.luckywarepro.musicintervals2anki.validation;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class IntegerRangeValidator implements FieldValidator {
    private final int minValue;
    private final int maxValue;

    private String errorTag;

    public IntegerRangeValidator(int minValue, int maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    public boolean isValid(String value) {
        if (!value.isEmpty()) {
            int intVal = Integer.parseInt(value);
            if (intVal < minValue) {
                errorTag = "below" + minValue;
                return false;
            }
            if (intVal > maxValue) {
                errorTag = "above" + maxValue;
                return false;
            }
        }
        return true;
    }

    @Override
    public String getErrorTag() {
        return errorTag;
    }
}
