package com.luckywarepro.musicintervals2anki.ui;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;

import com.luckywarepro.musicintervals2anki.R;

import java.io.IOException;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class SoundPlayer {
    private final MainActivity mainActivity;
    private MediaPlayer mediaPlayer;

    public SoundPlayer(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
    }

    public void play(Uri uri, String path) {
        if (uri == null) {
            mainActivity.showMsg(R.string.access_error, path);
            return;
        }
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(mainActivity, uri);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            mainActivity.showMsg(R.string.audio_playing_error);
        }
    }

    public void stop() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    public void release() {
        mediaPlayer.release();
        mediaPlayer = null;
    }
}
