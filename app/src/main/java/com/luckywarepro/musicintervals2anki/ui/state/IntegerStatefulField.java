package com.luckywarepro.musicintervals2anki.ui.state;

import android.content.SharedPreferences;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class IntegerStatefulField extends StatefulField<Integer> {
    public IntegerStatefulField(Supplier<Integer> supplier, Consumer<Integer> consumer, Integer defaultValue) {
        super(supplier, consumer, defaultValue);
    }

    @Override
    public void save(SharedPreferences.Editor preferenceEditor, String key) {
        preferenceEditor.putInt(key, getSupplier().get());
    }

    @Override
    public void restore(SharedPreferences preferences, String key) {
        getConsumer().accept(preferences.getInt(key, getDefaultValue()));
    }
}
