package com.luckywarepro.musicintervals2anki.ui.data;

import android.content.SharedPreferences;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public abstract class StatefulData<T> {
    private final Supplier<T> supplier;
    private final Consumer<T> consumer;
    private final T defaultValue;

    public StatefulData(Supplier<T> supplier, Consumer<T> consumer, T defaultValue) {
        this.supplier = supplier;
        this.consumer = consumer;
        this.defaultValue = defaultValue;
    }

    public abstract void save(SharedPreferences.Editor preferenceEditor, String key);

    public abstract void restore(SharedPreferences preferences, String key);

    protected Supplier<T> getSupplier() {
        return supplier;
    }

    protected Consumer<T> getConsumer() {
        return consumer;
    }

    protected T getDefaultValue() {
        return defaultValue;
    }
}
