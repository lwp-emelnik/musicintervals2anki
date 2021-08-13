package com.luckywarepro.musicintervals2anki.ui.data;

import android.content.SharedPreferences;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class StringStatefulData extends StatefulData<String> {
    public StringStatefulData(Supplier<String> supplier, Consumer<String> consumer, String defaultValue) {
        super(supplier, consumer, defaultValue);
    }

    @Override
    public void save(SharedPreferences.Editor preferenceEditor, String key) {
        preferenceEditor.putString(key, getSupplier().get());
    }

    @Override
    public void restore(SharedPreferences preferences, String key) {
        getConsumer().accept(preferences.getString(key, getDefaultValue()));
    }
}
