package com.luckywarepro.musicintervals2anki.ui;

import android.net.Uri;
import android.view.View;
import android.widget.Button;

import com.luckywarepro.musicintervals2anki.R;
import com.luckywarepro.musicintervals2anki.helper.AudioUtil;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class OnPlayAllClickListener implements View.OnClickListener {
    private final MainActivity mainActivity;
    private FilenameAdapter.UriPathName[] uriPathNames;
    private OnPlayClickListener[] listeners;
    private Button actionPlayAll;
    private final Runnable[] callbacks;
    private boolean isPlaying;

    public OnPlayAllClickListener(MainActivity mainActivity, FilenameAdapter.UriPathName[] uriPathNames, OnPlayClickListener[] listeners, Button actionPlayAll) {
        this.mainActivity = mainActivity;
        this.uriPathNames = uriPathNames;
        this.listeners = listeners;
        this.actionPlayAll = actionPlayAll;
        this.callbacks = new Runnable[uriPathNames.length + 1];
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setUriPathNames(FilenameAdapter.UriPathName[] uriPathNames) {
        this.uriPathNames = uriPathNames;
    }

    public void setListeners(OnPlayClickListener[] listeners) {
        this.listeners = listeners;
    }

    public void setActionPlayAll(Button actionPlayAll) {
        this.actionPlayAll = actionPlayAll;
    }

    @Override
    public void onClick(final View view) {
        for (OnPlayClickListener listener : listeners) {
            listener.stop();
        }

        if (isPlaying) {
            mainActivity.soundPlayer.stop();
            stop();
            return;
        }

        long timeout = 0;
        for (int i = 0; i < uriPathNames.length; i++) {
            FilenameAdapter.UriPathName uriPathName = uriPathNames[i];
            Uri uri = uriPathName.getUri();
            final OnPlayClickListener listener = listeners[i];
            Runnable callback = new Runnable() {
                @Override
                public void run() {
                    listener.onClick(view);
                }
            };
            mainActivity.handler.postDelayed(callback, timeout);
            timeout += AudioUtil.getDuration(mainActivity, uri);
            callbacks[i] = callback;
        }
        Runnable callback = new Runnable() {
            @Override
            public void run() {
                handleStopPlaying();
            }
        };
        mainActivity.handler.postDelayed(callback, timeout);
        callbacks[callbacks.length - 1] = callback;

        actionPlayAll.setText(R.string.stop);
        isPlaying = true;
    }

    void stop() {
        for (Runnable callback : callbacks) {
            mainActivity.handler.removeCallbacks(callback);
        }
        handleStopPlaying();
    }

    private void handleStopPlaying() {
        actionPlayAll.setText(R.string.play_all);
        isPlaying = false;
    }
}
