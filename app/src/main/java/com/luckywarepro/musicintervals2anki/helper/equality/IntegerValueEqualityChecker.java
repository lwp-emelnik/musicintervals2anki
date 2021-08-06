package com.luckywarepro.musicintervals2anki.helper.equality;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class IntegerValueEqualityChecker implements ValueEqualityChecker {
    @Override
    public boolean areEqual(String v1, String v2) {
        if (v1.isEmpty() && v2.isEmpty()) {
            return true;
        }
        try {
            return Integer.parseInt(v1) == Integer.parseInt(v2);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
