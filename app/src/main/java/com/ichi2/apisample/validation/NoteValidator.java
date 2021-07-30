package com.ichi2.apisample.validation;

import java.util.Map;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public interface NoteValidator extends Validator {
    boolean isValid(Map<String, String> data, Map<String, String> modelFields);
}
