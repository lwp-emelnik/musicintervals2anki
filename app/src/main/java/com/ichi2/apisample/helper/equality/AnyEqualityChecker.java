package com.ichi2.apisample.helper.equality;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class AnyEqualityChecker implements ValueEqualityChecker {
    @Override
    public boolean areEqual(String v1, String v2) {
        return true;
    }
}
