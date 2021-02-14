package com.example.headi.ui.timer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.headi.Constants;
import com.example.headi.R;
import com.example.headi.TimerForegroundService;
import com.example.headi.db.HeadiDBContract;
import com.example.headi.db.HeadiDBSQLiteHelper;
import com.example.headi.db.PainsCurserAdapter;


public class TimerFragment extends Fragment {

    private TimerViewModel timerViewModel;
    private View view;
    private Spinner pains_items;
    private Button button_start, button_save;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        timerViewModel = new ViewModelProvider(this).get(TimerViewModel.class);
        view = inflater.inflate(R.layout.fragment_timer, container, false);

        button_start = view.findViewById(R.id.timer_startOrStop_button);
        button_save = view.findViewById(R.id.timer_save_button);
        pains_items = (Spinner) view.findViewById(R.id.timer_pains_select);

        registerListeners();
        readFromDB();
        setUiAppearance(Constants.ACTION.INIT_ACTION);

        return view;
    }

    private void registerListeners() {

        // Start / Stop Button listener
        button_start.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TimerForegroundService.class);
            if (TimerForegroundService.isTimerRunning) {
                intent.setAction(Constants.ACTION.STOP_ACTION);
            } else {
                intent.setAction(Constants.ACTION.START_ACTION);
            }
            requireActivity().startService(intent);
            setUiAppearance(intent.getAction());
        });

        // Save Button listener
        button_save.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TimerForegroundService.class);
            intent.setAction(Constants.ACTION.SAVE_ACTION);
            requireActivity().startService(intent);
            setTimerTime(requireActivity().getString(R.string.timer_time));
        });

        // Spinner selected listener
        pains_items.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor prefEditor = sharedPref.edit();
                prefEditor.putLong(Constants.SHAREDPREFS.TIMER_SPINNER_PAINS, id);
                prefEditor.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        requireActivity().registerReceiver(broadcastReceiverTimer,
                new IntentFilter(Constants.BROADCAST.ACTION_CURRENT_TIME));
    }

    BroadcastReceiver broadcastReceiverTimer = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TextView mTimerView = (TextView) view.findViewById(R.id.timer_time);
            mTimerView.setText(intent.getExtras().get(Constants.BROADCAST.DATA_CURRENT_TIME).toString());
        }
    };

    private void setUiAppearance(String action) {
        switch (action) {
            case Constants.ACTION.INIT_ACTION:
                if (!TimerForegroundService.isTimerRunning) {
                    setButton(button_start, button_save, Constants.ACTION.STOP_ACTION);
                } else {
                    setButton(button_start, button_save, Constants.ACTION.START_ACTION);
                }
                // Set current timer time on init
                setTimerTime(TimerForegroundService.currentTime);
                break;
            case Constants.ACTION.START_ACTION:
                setButton(button_start, button_save, Constants.ACTION.START_ACTION);
                break;
            case Constants.ACTION.STOP_ACTION:
                setButton(button_start, button_save, Constants.ACTION.STOP_ACTION);
                break;
            default:
                setButton(button_start, button_save, Constants.ACTION.STOP_ACTION);
                break;
        }
    }

    private void setButton(Button start, Button save, String action) {
        if (action.equals(Constants.ACTION.STOP_ACTION)) {
            start.setText(requireActivity().getString(R.string.timer_start));
            start.setBackgroundColor(requireActivity().getColor(R.color.play));
            save.setEnabled(true);
        }

        if (action.equals(Constants.ACTION.START_ACTION)) {
            start.setText(requireActivity().getString(R.string.timer_stop));
            start.setBackgroundColor(requireActivity().getColor(R.color.stop));
            save.setEnabled(false);
        }
    }

    private void setTimerTime(CharSequence time) {
        TextView mTimerView = (TextView) view.findViewById(R.id.timer_time);
        mTimerView.setText(time);
    }

    private void readFromDB() {
        Context context = requireActivity();

        // Attach cursor adapter to the ListView
        HeadiDBSQLiteHelper helper = new HeadiDBSQLiteHelper(context);
        PainsCurserAdapter adapter = helper.readPainsFromDB(context);
        pains_items.setAdapter(adapter);

        // Set saved pain
        setSpinnerPain(adapter);
    }

    private void setSpinnerPain(PainsCurserAdapter adapter) {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        long id = sharedPref.getLong(Constants.SHAREDPREFS.TIMER_SPINNER_PAINS, 0);

        for (int i = 0; i < adapter.getCount(); i++) {
            Cursor cursor = (Cursor) adapter.getItem(i);
            String itemId = cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Pains._ID));

            if (itemId.equals(Long.toString(id))) {
                pains_items.setSelection(i);
            }
        }
    }

}