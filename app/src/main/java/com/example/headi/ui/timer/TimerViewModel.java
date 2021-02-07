package com.example.headi.ui.timer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TimerViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public TimerViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is timer fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}