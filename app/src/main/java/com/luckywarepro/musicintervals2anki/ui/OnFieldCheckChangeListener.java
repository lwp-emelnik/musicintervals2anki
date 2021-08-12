package com.luckywarepro.musicintervals2anki.ui;

import android.widget.CompoundButton;

import java.util.ArrayList;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class OnFieldCheckChangeListener implements CompoundButton.OnCheckedChangeListener {
    private final MainActivity mainActivity;

    private final CompoundButton[] checkBoxes;
    private final CompoundButton checkBoxAny;
    private boolean enableMultiple;

    public OnFieldCheckChangeListener(MainActivity mainActivity, CompoundButton[] checkBoxes, CompoundButton checkBoxAny) {
        this.mainActivity = mainActivity;
        this.checkBoxes = checkBoxes;
        this.checkBoxAny = checkBoxAny;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton.getId() == checkBoxAny.getId()) {
            if (b) {
                for (CompoundButton checkBox : checkBoxes) {
                    setChecked(checkBox,false);
                }
            } else if (enableMultiple) {
                for (CompoundButton checkBox : checkBoxes) {
                    if (checkBox.isChecked()) {
                        return;
                    }
                }
                for (CompoundButton checkBox : checkBoxes) {
                    setChecked(checkBox,true);
                }
            }
        } else if (b) {
            setChecked(checkBoxAny, false);
            if (!enableMultiple) {
                for (CompoundButton checkBox : checkBoxes) {
                    if (checkBox.getId() != compoundButton.getId()) {
                        setChecked(checkBox,false);
                    }
                }
            }
        }
        mainActivity.clearAddedFilenames();
        mainActivity.refreshExisting();
        mainActivity.refreshPermutations();
        mainActivity.refreshKeys();
    }

    public void setEnableMultiple(boolean enableMultiple) {
        if (!enableMultiple) {
            ArrayList<CompoundButton> checked = new ArrayList<>();
            for (CompoundButton checkBox : checkBoxes) {
                if (checkBox.isChecked()) {
                    checked.add(checkBox);
                }
            }
            if (checked.size() > 1) {
                for (CompoundButton checkBox : checked) {
                    setChecked(checkBox,false);
                }
                mainActivity.clearAddedFilenames();
                mainActivity.refreshExisting();
                mainActivity.refreshPermutations();
                mainActivity.refreshKeys();
            }
        }
        this.enableMultiple = enableMultiple;
    }

    private void setChecked(CompoundButton checkBox, boolean value) {
        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(value);
        checkBox.setOnCheckedChangeListener(this);
    }
}
