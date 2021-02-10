package com.example.headi.ui.timer;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.headi.R;

import java.util.concurrent.TimeUnit;

import timerx.Stopwatch;
import timerx.StopwatchBuilder;

public class TimerFragment extends Fragment {

    private TimerViewModel timerViewModel;
    private View view;
    private Stopwatch stopwatch;
    private Boolean startButtonClicked = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Context context = getActivity();

        timerViewModel = new ViewModelProvider(this).get(TimerViewModel.class);
        view = inflater.inflate(R.layout.fragment_timer, container, false);

        TextView mTextViewTimer = (TextView)view.findViewById(R.id.timer_time);
        stopwatch = new StopwatchBuilder()
                // Set the initial format
                .startFormat("HH:MM:SS")
                // Set the tick listener for displaying time
                .onTick(time -> mTextViewTimer.setText(time))
                // When time is equal to one hour, change format to "HH:MM:SS"
                .changeFormatWhen(1, TimeUnit.HOURS, "HH:MM:SS")
                .build();

        registerListeners(context);

        return view;
    }

    private void registerListeners(Context context) {
        // Start / Stop Button listener
        final Button button = view.findViewById(R.id.timer_startOrStop_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startButtonClicked) {
                    stopwatch.stop();
                    button.setText(context.getString(R.string.timer_start));
                    startButtonClicked = false;
                }
                else {
                    stopwatch.start();
                    button.setText(context.getString(R.string.timer_stop));
                    startButtonClicked = true;
                }
            }
        });
    }
}