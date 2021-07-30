package com.ichi2.apisample.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class TouchableButton extends Button {

    public TouchableButton(Context context) {
        super(context);
    }

    public TouchableButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchableButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
