package com.luckywarepro.musicintervals2anki.helper.search;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public interface SearchExpressionMaker {
    String getExpression(String value);
    boolean isDefinitive();
}
