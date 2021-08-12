package com.luckywarepro.musicintervals2anki.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import com.luckywarepro.musicintervals2anki.R;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class PlaybackButton extends Button {
    private static final int[] STATE_PLAYING = {R.attr.state_playing};

    private boolean isPlaying;

    public PlaybackButton(Context context) {
        super(context);
    }

    public PlaybackButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlaybackButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PlaybackButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isPlaying) {
            mergeDrawableStates(drawableState, STATE_PLAYING);
        }
        return drawableState;
    }

    public void setPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
        refreshDrawableState();
    }
}
