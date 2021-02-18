package com.example.headi.ui.medication;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MedicationViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public MedicationViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is export fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}