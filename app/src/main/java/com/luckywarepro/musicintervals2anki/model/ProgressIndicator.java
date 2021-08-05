package com.luckywarepro.musicintervals2anki.model;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public interface ProgressIndicator {
    void setMessage(int resId, Object ...formatArgs);
}
