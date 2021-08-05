package com.luckywarepro.musicintervals2anki.validation;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class EmptyValidator implements FieldValidator {
    @Override
    public boolean isValid(String value) {
        return !value.trim().isEmpty();
    }

    @Override
    public String getErrorTag() {
        return "empty";
    }
}