package com.luckywarepro.musicintervals2anki.helper;

import android.content.Context;
import android.net.Uri;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public interface ProcessibleFile {
    Uri getUri(Context context);

    void release(Context context);
}
