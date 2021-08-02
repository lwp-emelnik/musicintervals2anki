package com.luckywarepro.musicintervals2anki.model;

import com.luckywarepro.musicintervals2anki.helper.AnkiDroidHelper;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public interface AddingHandler {
    MusInterval add();

    MusInterval replace() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ValidationException;

    int mark() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ValidationException, MusInterval.NoteNotExistsException;

    int tag(String tag) throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ValidationException, MusInterval.NoteNotExistsException;

    void proceed() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ValidationException;
}
