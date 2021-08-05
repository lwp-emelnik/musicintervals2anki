package com.luckywarepro.musicintervals2anki.helper.equality;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class DoubleValueEqualityChecker implements ValueEqualityChecker {
    @Override
    public boolean areEqual(String v1, String v2) {
        if (v1.isEmpty() && v2.isEmpty()) {
            return true;
        }
        try {
            return Double.parseDouble(v1) == Double.parseDouble(v2);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
