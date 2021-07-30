package com.ichi2.apisample.ui;

import android.widget.RadioGroup;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class OnFieldRadioChangeListener implements RadioGroup.OnCheckedChangeListener {
    private final MainActivity mainActivity;

    public OnFieldRadioChangeListener(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        mainActivity.clearAddedFilenames();
        mainActivity.refreshExisting();
    }
}
