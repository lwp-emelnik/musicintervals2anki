package com.luckywarepro.musicintervals2anki.ui;

import android.widget.RadioGroup;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class OnFieldRadioChangeListener implements RadioGroup.OnCheckedChangeListener {
    private final MainActivity mainActivity;
    private final RadioGroup.OnCheckedChangeListener listener;

    public OnFieldRadioChangeListener(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        listener = null;
    }

    public OnFieldRadioChangeListener(MainActivity mainActivity, RadioGroup.OnCheckedChangeListener listener) {
        this.mainActivity = mainActivity;
        this.listener = listener;
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        if (listener != null) {
            listener.onCheckedChanged(radioGroup, i);
        }
        mainActivity.clearAddedFilenames();
        mainActivity.refreshExisting();
    }
}
