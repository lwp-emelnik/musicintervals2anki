package com.ichi2.apisample.validation;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public interface FieldValidator extends Validator {
    boolean isValid(String value);
}
