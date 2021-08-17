package com.luckywarepro.musicintervals2anki.ui;

import android.view.View;
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
    private boolean enableAny;

    private final String templateKey;

    public OnFieldCheckChangeListener(MainActivity mainActivity, CompoundButton[] checkBoxes, CompoundButton checkBoxAny, String templateKey) {
        this.mainActivity = mainActivity;
        this.checkBoxes = checkBoxes;
        this.checkBoxAny = checkBoxAny;
        this.templateKey = templateKey;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton.getId() == checkBoxAny.getId()) {
            if (b) {
                for (CompoundButton checkBox : checkBoxes) {
                    setChecked(checkBox, false);
                }
            } else if (enableMultiple) {
                for (CompoundButton checkBox : checkBoxes) {
                    if (checkBox.isChecked()) {
                        return;
                    }
                }
                for (CompoundButton checkBox : checkBoxes) {
                    setChecked(checkBox, true);
                }
            }
        } else {
            if (b) {
                setChecked(checkBoxAny, false);
                if (!enableMultiple) {
                    for (CompoundButton checkBox : checkBoxes) {
                        if (checkBox.getId() != compoundButton.getId()) {
                            setChecked(checkBox, false);
                        }
                    }
                }
            } else if (enableAny) {
                boolean checked = false;
                for (CompoundButton checkBox : checkBoxes) {
                    if (checkBox.isChecked()) {
                        checked = true;
                        break;
                    }
                }
                if (!checked) {
                    setChecked(checkBoxAny, true);
                }
            }
        }
        for (View view : checkBoxes) {
            mainActivity.fieldEdited(String.format(templateKey, view.getId()));
        }
        commit();
    }

    public void clear() {
        for (CompoundButton checkBox : checkBoxes) {
            setChecked(checkBox, false);
        }
        if (enableAny) {
            setChecked(checkBoxAny, true);
        }
        commit();
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
                    setChecked(checkBox, false);
                }
                commit();
            }
        }
        this.enableMultiple = enableMultiple;
    }

    public void setEnableAny(boolean enableAny) {
        if (!enableAny) {
            setChecked(checkBoxAny, false);
        } else {
            boolean checked = false;
            for (CompoundButton checkBox : checkBoxes) {
                if (checkBox.isChecked()) {
                    checked = true;
                    break;
                }
            }
            if (!checked) {
                setChecked(checkBoxAny, true);
            }
        }
        this.enableAny = enableAny;
        commit();
    }

    private void commit() {
        mainActivity.clearAddedFilenames();
        mainActivity.refreshExisting();
        mainActivity.refreshPermutations();
        mainActivity.refreshKeys();
    }

    private void setChecked(CompoundButton checkBox, boolean value) {
        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(value);
        checkBox.setOnCheckedChangeListener(this);
    }
}
