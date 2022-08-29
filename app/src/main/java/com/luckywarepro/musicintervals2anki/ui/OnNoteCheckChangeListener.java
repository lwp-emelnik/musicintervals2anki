package com.luckywarepro.musicintervals2anki.ui;

import android.widget.CompoundButton;

public class OnNoteCheckChangeListener extends OnFieldCheckChangeListener {
    private final IntervalToggleButton unisonIntervalToggleButton;

    public OnNoteCheckChangeListener(MainActivity mainActivity, CompoundButton[] checkBoxes, CompoundButton checkBoxAny, String templateKey, IntervalToggleButton unisonIntervalToggleButton) {
        super(mainActivity, checkBoxes, checkBoxAny, templateKey);
        this.unisonIntervalToggleButton = unisonIntervalToggleButton;
    }

    @Override
    protected void check(CompoundButton compoundButton) {
        super.check(compoundButton);
        unisonIntervalToggleButton.setHighlighted(true);
    }

    @Override
    protected void uncheckAll() {
        super.uncheckAll();
        unisonIntervalToggleButton.setHighlighted(false);
    }

    @Override
    protected void checkAll() {
        super.checkAll();
        unisonIntervalToggleButton.setHighlighted(true);
    }

    @Override
    protected void uncheck(CompoundButton compoundButton) {
        super.uncheck(compoundButton);
        if (checkedCount == 0) {
            unisonIntervalToggleButton.setHighlighted(false);
        }
    }
}
