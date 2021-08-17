package com.luckywarepro.musicintervals2anki.ui;

import android.widget.RadioGroup;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class OnFieldRadioChangeListener implements RadioGroup.OnCheckedChangeListener {
    private final MainActivity mainActivity;
    private final RadioGroup.OnCheckedChangeListener listener;

    private final String key;

    public OnFieldRadioChangeListener(MainActivity mainActivity, String key) {
        this.mainActivity = mainActivity;
        listener = null;
        this.key = key;
    }

    public OnFieldRadioChangeListener(MainActivity mainActivity, RadioGroup.OnCheckedChangeListener listener, String key) {
        this.mainActivity = mainActivity;
        this.listener = listener;
        this.key = key;
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        if (listener != null) {
            listener.onCheckedChanged(radioGroup, i);
        }
        mainActivity.fieldEdited(key);
        mainActivity.clearAddedFilenames();
        mainActivity.refreshExisting();
    }
}
