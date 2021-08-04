package com.luckywarepro.musicintervals2anki.validation;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class PositiveDecimalValidator implements FieldValidator {
    @Override
    public boolean isValid(String value) {
        return value.matches("^\\d*$|^(?=^\\d*\\.\\d*$)(?=^(?!\\.$).*$).*$");
    }

    @Override
    public String getErrorTag() {
        return "invalid";
    }
}
