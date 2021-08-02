package com.luckywarepro.musicintervals2anki.helper.equality;

import java.util.Map;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public interface EqualityChecker {
    boolean areEqual(Map<String, String> data1, Map<String, String> data2);
}
