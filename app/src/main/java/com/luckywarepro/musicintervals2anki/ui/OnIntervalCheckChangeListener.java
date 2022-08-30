package com.luckywarepro.musicintervals2anki.ui;

import android.widget.CompoundButton;

import java.util.Arrays;

public class OnIntervalCheckChangeListener extends OnFieldCheckChangeListener {
    private final OnNoteCheckChangeListener onNoteCheckChangeListener;

    private Boolean ascending = null;
    private NoteToggleButton hintedNote;

    public OnIntervalCheckChangeListener(MainActivity mainActivity, CompoundButton[] checkBoxes, CompoundButton checkBoxAny, String templateKey, OnNoteCheckChangeListener onNoteCheckChangeListener) {
        super(mainActivity, checkBoxes, checkBoxAny, templateKey);
        this.onNoteCheckChangeListener = onNoteCheckChangeListener;
    }

    private void hint() {
        if (onNoteCheckChangeListener.checked.size() == 1 && checked.size() == 1 && ascending != null) {
            CompoundButton check = checked.get(0);
            CompoundButton[] noteChecks = onNoteCheckChangeListener.checkBoxes;
            int hintIndex = Arrays.asList(noteChecks).indexOf(onNoteCheckChangeListener.checked.get(0))
                    + Arrays.asList(checkBoxes).indexOf(check) * (ascending ? 1 : -1);
            if (hintIndex >= 0 && hintIndex < noteChecks.length) {
                hintedNote = (NoteToggleButton) noteChecks[hintIndex];
                hintedNote.setHintFor(check.getText().toString());
            }
        }
    }

    private void unhint() {
        if (hintedNote != null) {
            hintedNote.setHintFor(null);
            hintedNote = null;
        }
    }

    @Override
    protected void check(CompoundButton compoundButton) {
        super.check(compoundButton);
        unhint();
        hint();
    }

    @Override
    protected void uncheck(CompoundButton compoundButton) {
        super.uncheck(compoundButton);
        unhint();
        hint();
    }

    @Override
    protected void uncheckAll() {
        super.uncheckAll();
        unhint();
    }

    public void setAscending(Boolean ascending) {
        this.ascending = ascending;
        unhint();
        hint();
    }
}
