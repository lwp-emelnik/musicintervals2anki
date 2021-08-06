package com.luckywarepro.musicintervals2anki.ui;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class GetStringArgs {
    private final int resId;
    private final Object[] formatArgs;

    public GetStringArgs(int resId, Object... formatArgs) {
        this.resId = resId;
        this.formatArgs = formatArgs;
    }

    public int getResId() {
        return resId;
    }

    public Object[] getFormatArgs() {
        return formatArgs;
    }
}
