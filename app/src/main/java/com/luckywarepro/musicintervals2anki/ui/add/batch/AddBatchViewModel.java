package com.luckywarepro.musicintervals2anki.ui.add.batch;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AddBatchViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public AddBatchViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is add batch fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}