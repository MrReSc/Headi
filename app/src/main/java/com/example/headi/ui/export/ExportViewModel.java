package com.example.headi.ui.export;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ExportViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ExportViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is export fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}