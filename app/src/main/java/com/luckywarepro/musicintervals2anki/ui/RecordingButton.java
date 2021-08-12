package com.luckywarepro.musicintervals2anki.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import com.luckywarepro.musicintervals2anki.R;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class RecordingButton extends Button {
    private static final int[] STATE_RECORDING = {R.attr.state_recording};

    private boolean isRecording;

    public RecordingButton(Context context) {
        super(context);
    }

    public RecordingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecordingButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RecordingButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isRecording) {
            mergeDrawableStates(drawableState, STATE_RECORDING);
        }
        return drawableState;
    }

    public void setRecording(boolean isRecording) {
        this.isRecording = isRecording;
        refreshDrawableState();
    }
}