package com.ichi2.apisample.model;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public interface ProgressIndicator {
    void setMessage(int resId, Object ...formatArgs);
}
