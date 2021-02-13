package com.example.headi.ui.timer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.headi.Constants;
import com.example.headi.MainActivity;
import com.example.headi.R;
import com.example.headi.TimerForegroundService;
import com.example.headi.db.HeadiDBSQLiteHelper;


public class TimerFragment extends Fragment {

    private TimerViewModel timerViewModel;
    private View view;
    private Spinner PainsItems;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Context context = getActivity();

        timerViewModel = new ViewModelProvider(this).get(TimerViewModel.class);
        view = inflater.inflate(R.layout.fragment_timer, container, false);

        registerListeners();
        setStateTimerButtons();

        PainsItems = (Spinner) view.findViewById(R.id.timer_pains_select);
        readFromDB();

        return view;
    }

    private void registerListeners() {

        // Start / Stop Button listener
        final Button button_start = view.findViewById(R.id.timer_startOrStop_button);
        button_start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TimerForegroundService.class);
                if (button_start.getText() == getActivity().getString(R.string.timer_stop)) {
                    intent.setAction(Constants.ACTION.STOP_ACTION);
                }
                else {
                    intent.setAction(Constants.ACTION.START_ACTION);
                }
                getActivity().startService(intent);
                setStateTimerButtons(intent.getAction());
            }

        });

        // Save Button listener
        final Button button_save = view.findViewById(R.id.timer_save_button);
        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TimerForegroundService.class);
                intent.setAction(Constants.ACTION.SAVE_ACTION);
                getActivity().startService(intent);
            }
        });
    }

    private void setStateTimerButtons(String action) {
        Context context = getActivity();

        final Button button_start = view.findViewById(R.id.timer_startOrStop_button);
        final Button button_save = view.findViewById(R.id.timer_save_button);
        boolean timerIsRunning = ((MainActivity)getActivity()).isServiceRunning(TimerForegroundService.class);

        if (!timerIsRunning | action.equals(Constants.ACTION.STOP_ACTION)) {
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

    private void setStateTimerButtons() {
        setStateTimerButtons("");
    }

    private void readFromDB() {
        Context context = getActivity();

        // Attach cursor adapter to the ListView
        HeadiDBSQLiteHelper helper = new HeadiDBSQLiteHelper(context);
        PainsItems.setAdapter(helper.readPainsFromDB(context));
    }
}