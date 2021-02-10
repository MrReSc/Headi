package com.example.headi.ui.timer;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.headi.MainActivity;
import com.example.headi.R;
import com.example.headi.db.HeadiDBSQLiteHelper;


public class TimerFragment extends Fragment {

    private TimerViewModel timerViewModel;
    private View view;
    private StopwatchHelper stopwatch;
    private Spinner PainsItems;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Context context = getActivity();

        timerViewModel = new ViewModelProvider(this).get(TimerViewModel.class);
        view = inflater.inflate(R.layout.fragment_timer, container, false);

        stopwatch = ((MainActivity) getActivity()).stopwatch;
        setStateTimerButtons(context, stopwatch.getStartButtonClicked());
        registerListeners(context);

        PainsItems = (Spinner) view.findViewById(R.id.timer_pains_select);
        readFromDB();

        return view;
    }

    private void registerListeners(Context context) {

        // Start / Stop Button listener
        final Button button_start = view.findViewById(R.id.timer_startOrStop_button);
        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (stopwatch.getStartButtonClicked()) {
                    stopwatch.getStopwatch().stop();
                    stopwatch.setStartButtonClicked(false);
                } else {
                    stopwatch.getStopwatch().start();
                    stopwatch.setStartButtonClicked(true);
                }
                setStateTimerButtons(context, stopwatch.getStartButtonClicked());
            }
        });

        // Save Button listener
        final Button button_save = view.findViewById(R.id.timer_save_button);
        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        stopwatch.timer.observe(getActivity(), new Observer<CharSequence>() {
            @Override
            public void onChanged(CharSequence changedValue) {
                TextView mTextViewTimer = view.findViewById(R.id.timer_time);
                mTextViewTimer.setText(changedValue);
            }
        });
    }

    private void setStateTimerButtons(Context context, boolean state) {
        final Button button_start = view.findViewById(R.id.timer_startOrStop_button);
        final Button button_save = view.findViewById(R.id.timer_save_button);
        if (!state) {
            button_start.setText(context.getString(R.string.timer_start));
            button_start.setBackgroundColor(context.getColor(R.color.play));
            button_save.setEnabled(true);
        }
        else {
            button_start.setText(context.getString(R.string.timer_stop));
            button_start.setBackgroundColor(context.getColor(R.color.stop));
            button_save.setEnabled(false);
        }
    }

    private void readFromDB() {
        Context context = getActivity();

        // Attach cursor adapter to the ListView
        HeadiDBSQLiteHelper helper = new HeadiDBSQLiteHelper(context);
        PainsItems.setAdapter(helper.readPainsFromDB(context));
    }
}