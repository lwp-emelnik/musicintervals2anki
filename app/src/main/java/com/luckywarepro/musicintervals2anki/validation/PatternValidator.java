package com.luckywarepro.musicintervals2anki.validation;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class PatternValidator implements FieldValidator {
    private final String pattern;

    public PatternValidator(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public boolean isValid(String value) {
        return value.matches(pattern);
    }

    @Override
    public String getErrorTag() {
        return "invalid";
    }
}
