package com.luckywarepro.musicintervals2anki.ui;

import android.widget.CompoundButton;

import java.util.Arrays;

public class OnNoteCheckChangeListener extends OnFieldCheckChangeListener {
    private final IntervalToggleButton unisonIntervalToggleButton;
    private final IntervalToggleButton[] intervalToggleButtons;
    private boolean intervalsHinted;
    private Boolean ascending = null;
    private Callback nonRadioModeCallback;
    private Callback radioModeCallback;

    public OnNoteCheckChangeListener(MainActivity mainActivity, CompoundButton[] checkBoxes, CompoundButton checkBoxAny, String templateKey, IntervalToggleButton[] intervalToggleButtons) {
        super(mainActivity, checkBoxes, checkBoxAny, templateKey);
        unisonIntervalToggleButton = intervalToggleButtons[0];
        this.intervalToggleButtons = intervalToggleButtons;
    }

    private void hintIntervals() {
        if (checked.size() == 1 && ascending != null) {
            int index = Arrays.asList(checkBoxes).indexOf(checked.get(0));
            for (int i = 0; i < intervalToggleButtons.length; i++) {
                intervalToggleButtons[ascending ? i : intervalToggleButtons.length - i - 1]
                        .setHintFor(checkBoxes[(index + i) % checkBoxes.length].getText().toString());
            }
            intervalsHinted = true;
            if (radioModeCallback != null) {
                radioModeCallback.run();
            }
        }
    }

    private void unhintIntervals() {
        if (intervalsHinted) {
            for (IntervalToggleButton intervalToggleButton : intervalToggleButtons) {
                intervalToggleButton.setHintFor(null);
            }
            intervalsHinted = false;
            if (nonRadioModeCallback != null) {
                nonRadioModeCallback.run();
            }
        }
    }

    @Override
    protected void check(CompoundButton compoundButton) {
        super.check(compoundButton);
        unisonIntervalToggleButton.setHighlighted(true);
        hintIntervals();
        if (checked.size() > 1) {
            unhintIntervals();
        }
    }

    @Override
    protected void uncheckAll() {
        super.uncheckAll();
        unisonIntervalToggleButton.setHighlighted(false);
        unhintIntervals();
    }

    @Override
    protected void checkAll() {
        super.checkAll();
        unisonIntervalToggleButton.setHighlighted(true);
    }

    @Override
    protected void uncheck(CompoundButton compoundButton) {
        super.uncheck(compoundButton);
        hintIntervals();
        if (checked.size() == 0) {
            unisonIntervalToggleButton.setHighlighted(false);
            unhintIntervals();
        }
    }

    public void setAscending(Boolean ascending) {
        this.ascending = ascending;
        if (ascending == null) {
            unhintIntervals();
        } else {
            hintIntervals();
        }
    }

    public void setNonRadioModeCallback(Callback nonRadioModeCallback) {
        this.nonRadioModeCallback = nonRadioModeCallback;
    }

    public void setRadioModeCallback(Callback radioModeCallback) {
        this.radioModeCallback = radioModeCallback;
    }

    interface Callback {
        void run();
    }
}
