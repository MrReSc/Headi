package com.example.headi.ui.timer;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
    BroadcastReceiver broadcastReceiverTimer = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TextView mTimerView = view.findViewById(R.id.timer_time);
            mTimerView.setText(intent.getExtras().get(Constants.BROADCAST.DATA_CURRENT_TIME).toString());
        }
    };
    private Spinner pains_items;
    private Button button_start, button_save;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        timerViewModel = new ViewModelProvider(this).get(TimerViewModel.class);
        view = inflater.inflate(R.layout.fragment_timer, container, false);

        button_start = view.findViewById(R.id.timer_startOrStop_button);
        button_save = view.findViewById(R.id.timer_save_button);
        pains_items = view.findViewById(R.id.timer_pains_select);

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
            openSaveDialog();
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
        switch (action) {
            case Constants.ACTION.STOP_ACTION:
                start.setText(requireActivity().getString(R.string.timer_start));
                start.setBackgroundColor(requireActivity().getColor(R.color.play));
                save.setEnabled(true);
                break;
            case Constants.ACTION.START_ACTION:
                start.setText(requireActivity().getString(R.string.timer_stop));
                start.setBackgroundColor(requireActivity().getColor(R.color.stop));
                save.setEnabled(false);
                break;
            default:
                save.setEnabled(TimerForegroundService.isTimerRunning);
                break;
        }
    }

    private void setTimerTime(CharSequence time) {
        TextView mTimerView = view.findViewById(R.id.timer_time);
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

    private void saveToDB(String region, String description) {
        Context context = requireActivity();
        timerForegroundServiceEndAction();

        // Save to DB
        SQLiteDatabase database = new HeadiDBSQLiteHelper(context).getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(HeadiDBContract.Diary.COLUMN_START_DATE, TimerForegroundService.startDate);
        values.put(HeadiDBContract.Diary.COLUMN_END_DATE, TimerForegroundService.endDate);
        values.put(HeadiDBContract.Diary.COLUMN_DURATION, TimerForegroundService.elapsedTime);
        values.put(HeadiDBContract.Diary.COLUMN_REGION, region);
        values.put(HeadiDBContract.Diary.COLUMN_DESCRIPTION, description);
        values.put(HeadiDBContract.Diary.COLUMN_PAIN_ID, pains_items.getSelectedItemId());

        database.insert(HeadiDBContract.Diary.TABLE_NAME, null, values);

        Toast.makeText(context, context.getString(R.string.new_diary_added), Toast.LENGTH_SHORT).show();
    }

    private void timerForegroundServiceEndAction() {
        Intent intent = new Intent(getActivity(), TimerForegroundService.class);
        intent.setAction(Constants.ACTION.END_ACTION);
        requireActivity().startService(intent);
        setTimerTime(requireActivity().getString(R.string.timer_time));
    }

    private void openSaveDialog() {
        Context context = requireActivity();

        // Create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.title_diary_save));

        // set the custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.fragment_save_diary_dialog, null);
        builder.setView(customLayout);

        // add save button
        builder.setPositiveButton(context.getString(R.string.save_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText diaryDescription = customLayout.findViewById(R.id.diary_description);
                EditText diaryRegion = customLayout.findViewById(R.id.diary_region);
                saveToDB(diaryRegion.getText().toString(), diaryDescription.getText().toString());
            }
        });

        // add delete button
        builder.setNegativeButton(context.getString(R.string.delete_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                timerForegroundServiceEndAction();
            }
        });

        // add cancel button
        builder.setNeutralButton(context.getString(R.string.cancel_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
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