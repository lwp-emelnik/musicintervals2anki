package com.luckywarepro.musicintervals2anki.ui;

import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.ArrayList;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class OnFieldCheckChangeListener implements CompoundButton.OnCheckedChangeListener {
    private final MainActivity mainActivity;

    private final CheckBox[] checkBoxes;
    private final CheckBox checkBoxAny;
    private boolean enableMultiple;

    public OnFieldCheckChangeListener(MainActivity mainActivity, CheckBox[] checkBoxes, CheckBox checkBoxAny, boolean enableMultiple) {
        this.mainActivity = mainActivity;
        this.checkBoxes = checkBoxes;
        this.checkBoxAny = checkBoxAny;
        this.enableMultiple = enableMultiple;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton.getId() == checkBoxAny.getId()) {
            if (b) {
                for (CheckBox checkBox : checkBoxes) {
                    setChecked(checkBox,false);
                }
            } else if (enableMultiple) {
                for (CheckBox checkBox : checkBoxes) {
                    if (checkBox.isChecked()) {
                        return;
                    }
                }
                for (CheckBox checkBox : checkBoxes) {
                    setChecked(checkBox,true);
                }
            }
        } else if (b) {
            setChecked(checkBoxAny, false);
            if (!enableMultiple) {
                for (CheckBox checkBox : checkBoxes) {
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
            ArrayList<CheckBox> checked = new ArrayList<>();
            for (CheckBox checkBox : checkBoxes) {
                if (checkBox.isChecked()) {
                    checked.add(checkBox);
                }
            }
            if (checked.size() > 1) {
                for (CheckBox checkBox : checked) {
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

    private void setChecked(CheckBox checkBox, boolean value) {
        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(value);
        checkBox.setOnCheckedChangeListener(this);
    }
}
