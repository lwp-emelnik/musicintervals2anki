package com.luckywarepro.musicintervals2anki.ui;

import android.view.View;
import android.widget.CompoundButton;

import java.util.ArrayList;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class OnFieldCheckChangeListener implements CompoundButton.OnCheckedChangeListener {
    private final MainActivity mainActivity;

    protected final CompoundButton[] checkBoxes;
    private final CompoundButton checkBoxAny;
    private boolean enableMultiple;
    private boolean enableAny;
    protected final ArrayList<CompoundButton> checked = new ArrayList<>();

    private final String templateKey;

    public OnFieldCheckChangeListener(MainActivity mainActivity, CompoundButton[] checkBoxes, CompoundButton checkBoxAny, String templateKey) {
        this.mainActivity = mainActivity;
        this.checkBoxes = checkBoxes;
        this.checkBoxAny = checkBoxAny;
        this.templateKey = templateKey;
    }

    protected void check(CompoundButton compoundButton) {
        setChecked(checkBoxAny, false);
        checked.add(compoundButton);
        if (!enableMultiple) {
            for (CompoundButton checkBox : checkBoxes) {
                if (checkBox.getId() != compoundButton.getId()) {
                    setChecked(checkBox, false);
                    checked.remove(checkBox);
                }
            }
        }
    }

    protected void uncheckAll() {
        for (CompoundButton checkBox : checkBoxes) {
            setChecked(checkBox, false);
            checked.remove(checkBox);
        }
    }

    protected void checkAll() {
        checked.clear();
        for (CompoundButton checkBox : checkBoxes) {
            setChecked(checkBox, true);
            checked.add(checkBox);
        }
    }

    protected void uncheck(CompoundButton compoundButton) {
        checked.remove(compoundButton);
        if (enableAny) {
            if (checked.size() == 0) {
                setChecked(checkBoxAny, true);
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton.getId() == checkBoxAny.getId()) {
            if (b) {
                uncheckAll();
            } else if (enableMultiple) {
                if (checked.size() > 0) {
                    return;
                }
                checkAll();
            }
        } else {
            if (b) {
                check(compoundButton);
            } else {
                uncheck(compoundButton);
            }
        }
        for (View view : checkBoxes) {
            mainActivity.fieldEdited(String.format(templateKey, view.getId()));
        }
        commit();
    }

    public void clear() {
        uncheckAll();
        if (enableAny) {
            setChecked(checkBoxAny, true);
        }
        commit();
    }

    public void setEnableMultiple(boolean enableMultiple) {
        if (!enableMultiple) {
            if (checked.size() > 1) {
                uncheckAll();
                commit();
            }
        }
        this.enableMultiple = enableMultiple;
    }

    public void setEnableAny(boolean enableAny) {
        if (!enableAny) {
            setChecked(checkBoxAny, false);
        } else {
            if (checked.size() == 0) {
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
