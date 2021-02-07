package com.example.headi.ui.stats;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StatsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public StatsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is stats fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}