package com.luckywarepro.musicintervals2anki.helper.search;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class AnySearchExpressionMaker implements SearchExpressionMaker {
    @Override
    public String getExpression(String value) {
        return "%";
    }

    @Override
    public boolean isDefinitive() {
        return false;
    }
}
