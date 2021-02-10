package com.example.headi.ui.timer;

import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.TimeUnit;

import timerx.Stopwatch;
import timerx.StopwatchBuilder;

public class StopwatchHelper {

    private Stopwatch stopwatch;
    private Boolean startButtonClicked = false;
    public MutableLiveData<CharSequence> timer = new MutableLiveData<>();

    public StopwatchHelper() {
        stopwatch = new StopwatchBuilder()
                // Set the initial format
                .startFormat("HH:MM:SS")
                // Set the tick listener for displaying time
                .onTick(time -> timer.setValue(time))
                // When time is equal to one hour, change format to "HH:MM:SS"
                .changeFormatWhen(1, TimeUnit.HOURS, "HH:MM:SS")
                .build();
    }

    public Stopwatch getStopwatch() {
        return stopwatch;
    }

    public void setStartButtonClicked(boolean val) {
        startButtonClicked = val;
    }

    public boolean getStartButtonClicked() {
        return startButtonClicked;
    }
}
