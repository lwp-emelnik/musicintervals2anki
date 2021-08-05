package com.luckywarepro.musicintervals2anki.model;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public interface AddingPrompter {
    void promptAddDuplicate(MusInterval[] existingMis, AddingHandler handler);

    void addingFinished(MusInterval.AddingResult addingResult);

    void processException(Throwable t);
}
