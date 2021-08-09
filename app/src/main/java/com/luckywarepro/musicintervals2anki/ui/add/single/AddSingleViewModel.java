package com.luckywarepro.musicintervals2anki.ui.add.single;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AddSingleViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public AddSingleViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("sample.mp3");
    }

    public LiveData<String> getText() {
        return mText;
    }
}