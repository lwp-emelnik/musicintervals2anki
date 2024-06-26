package com.luckywarepro.musicintervals2anki.helper.search;

import java.util.Locale;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class DoubleSearchExpressionMaker implements SearchExpressionMaker {
    @Override
    public String getExpression(String value) {
        if (value.isEmpty()) {
            return "%";
        }
        double number;
        try {
            number = Double.parseDouble(value);
            if (number % 1 == 0) {
                return String.format(Locale.US, "%%%d%%", (int) number);
            } else {
                return String.format(Locale.US, "%%%s%%", number);
            }
        } catch (NumberFormatException e) {
            return value;
        }
    }

    @Override
    public boolean isDefinitive() {
        return false;
    }
}
