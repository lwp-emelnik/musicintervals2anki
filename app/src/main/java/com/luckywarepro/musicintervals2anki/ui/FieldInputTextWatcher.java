package com.luckywarepro.musicintervals2anki.ui;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class FieldInputTextWatcher implements TextWatcher {
    private final MainActivity mainActivity;
    private String prev;

    public FieldInputTextWatcher(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        prev = charSequence.toString();
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        String curr = charSequence.toString();
        if (!curr.equalsIgnoreCase(prev)) {
            mainActivity.clearAddedFilenames();
            mainActivity.refreshExisting();
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }
}