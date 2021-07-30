package com.ichi2.apisample.validation;

import com.ichi2.apisample.helper.AnkiDroidHelper;

import java.util.Map;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public interface FixableNoteValidator extends NoteValidator {
    boolean fix(long modelId, long noteId, Map<String, String> data, Map<String, String> modelFields, AnkiDroidHelper helper);
}
