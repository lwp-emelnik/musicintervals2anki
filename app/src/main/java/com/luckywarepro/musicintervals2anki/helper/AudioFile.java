package com.luckywarepro.musicintervals2anki.helper;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class AudioFile implements ProcessibleFile {
    private static final String LOG_TAG = "AudioFile";

    private final String tempAudioFilePath = "/tempExtractedAudio.mp3";

    private final String uriString;

    public AudioFile(String uriString) {
        this.uriString = uriString;
    }

    public String getUriString() {
        return uriString;
    }

    @Override
    public Uri getUri(Context context) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = Uri.parse(uriString);
        String type = resolver.getType(uri);
        if (type != null && type.startsWith("video")) {
            return AudioUtil.extractFromVideo(context, uri, context.getExternalCacheDir() + tempAudioFilePath);
        }
        return uri;
    }

    @Override
    public void release(Context context) {
        File tempAudioFile = new File(context.getExternalCacheDir() + tempAudioFilePath);
        if (tempAudioFile.exists() && !tempAudioFile.delete()) {
            Log.e(LOG_TAG, "Could not delete extracted audio file");
        }
    }
}
