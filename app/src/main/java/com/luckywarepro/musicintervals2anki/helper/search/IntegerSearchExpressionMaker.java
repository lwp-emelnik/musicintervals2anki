package com.luckywarepro.musicintervals2anki.helper.search;

import java.util.Locale;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class IntegerSearchExpressionMaker implements SearchExpressionMaker {
    @Override
    public String getExpression(String value) {
        value = value.trim();
        if (value.isEmpty()) {
            return "%";
        }
        int number;
        try {
            number = Integer.parseInt(value);
            return String.format(Locale.US, "%%%d%%", number);
        } catch (NumberFormatException e) {
            return value;
        }
    }

    @Override
    public boolean isDefinitive() {
        return false;
    }
}
