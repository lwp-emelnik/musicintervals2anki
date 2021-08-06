package com.luckywarepro.musicintervals2anki.ui;

import android.view.View;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class OnGroupPlayClickListener implements View.OnClickListener {
    private final OnPlayClickListener listener;
    private final OnPlayClickListener[] groupListeners;
    private final OnPlayAllClickListener allListener;

    public OnGroupPlayClickListener(OnPlayClickListener listener, OnPlayClickListener[] groupListeners, OnPlayAllClickListener allListener) {
        this.listener = listener;
        this.groupListeners = groupListeners;
        this.allListener = allListener;
    }

    @Override
    public void onClick(View view) {
        if (allListener.isPlaying()) {
            allListener.stop();
        }
        listener.onClick(view);
        for (OnPlayClickListener groupListener : groupListeners) {
            if (listener != groupListener) {
                groupListener.stop();
            }
        }
    }
}
