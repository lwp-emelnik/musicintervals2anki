package com.luckywarepro.musicintervals2anki.ui.state;

import android.content.SharedPreferences;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class StringSetStatefulField extends StatefulField<Set<String>> {
    public StringSetStatefulField(Supplier<Set<String>> supplier, Consumer<Set<String>> consumer, Set<String> defaultValue) {
        super(supplier, consumer, defaultValue);
    }

    @Override
    public void save(SharedPreferences.Editor preferenceEditor, String key) {
        preferenceEditor.putStringSet(key, getSupplier().get());
    }

    @Override
    public void restore(SharedPreferences preferences, String key) {
        getConsumer().accept(preferences.getStringSet(key, preferences.getStringSet(key, getDefaultValue())));
    }
}
