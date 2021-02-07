package com.example.headi.ui.pains;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PainsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public PainsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is pains fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}