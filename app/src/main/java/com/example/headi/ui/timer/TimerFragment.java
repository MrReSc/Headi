package com.example.headi.ui.timer;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.headi.Constants;
import com.example.headi.R;
import com.example.headi.TimerForegroundService;
import com.example.headi.db.HeadiDBContract;
import com.example.headi.db.HeadiDBSQLiteHelper;
import com.example.headi.db.MedicationsCourserAdapter;
import com.example.headi.db.PainsCourserIconAdapter;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

import tech.picnic.fingerpaintview.FingerPaintImageView;


public class TimerFragment extends Fragment {

    private View view;
    private Spinner pains_items;
    private Button button_start;

    final BroadcastReceiver broadcastReceiverTimer = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TextView mTimerView = view.findViewById(R.id.timer_time);
            mTimerView.setText(intent.getExtras().get(Constants.BROADCAST.DATA_CURRENT_TIME).toString());
        }
    };

    public static byte[] getDrawableAsByteArray(Drawable d) {
        Bitmap bitmap;

        if (d instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) d).getBitmap();
            bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 3, bitmap.getHeight() / 3, false);
        } else {
            bitmap = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 3, bitmap.getHeight() / 3, false);
            Canvas canvas = new Canvas(bitmap);
            d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            d.draw(canvas);
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_timer, container, false);

        button_start = view.findViewById(R.id.timer_startOrStop_button);
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
                openSaveDialog();
            } else {
                intent.setAction(Constants.ACTION.START_ACTION);
            }
            requireActivity().startService(intent);
            setUiAppearance(intent.getAction());
        });

        // Spinner selected listener
        pains_items.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor prefEditor = sharedPref.edit();
                prefEditor.putLong(Constants.SHAREDPREFS.TIMER_SPINNER_PAINS, id);
                prefEditor.apply();
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
                    setButton(button_start, Constants.ACTION.STOP_ACTION);
                } else {
                    setButton(button_start, Constants.ACTION.START_ACTION);
                }
                // Set current timer time on init
                setTimerTime(TimerForegroundService.currentTime);
                break;
            case Constants.ACTION.START_ACTION:
                setButton(button_start, Constants.ACTION.START_ACTION);
                break;
            case Constants.ACTION.STOP_ACTION:
            default:
                setButton(button_start, Constants.ACTION.STOP_ACTION);
                break;
        }
    }

    private void setButton(Button start, String action) {
        switch (action) {
            case Constants.ACTION.STOP_ACTION:
                start.setText(requireActivity().getString(R.string.timer_start));
                start.setBackgroundColor(requireActivity().getColor(R.color.button_play));
                break;
            case Constants.ACTION.START_ACTION:
                start.setText(requireActivity().getString(R.string.timer_stop));
                start.setBackgroundColor(requireActivity().getColor(R.color.button_stop));
                break;
            default:
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
        PainsCourserIconAdapter adapter = helper.readPainsWithIconFromDB(context);
        pains_items.setAdapter(adapter);

        // Set saved pain
        setSpinnerPain(adapter);
    }

    private void saveToDB(Drawable region, String description, String medication, int strength, int medicationAmount) {
        Context context = requireActivity();
        timerForegroundServiceEndAction();

        // Save to DB
        SQLiteDatabase database = new HeadiDBSQLiteHelper(context).getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(HeadiDBContract.Diary.COLUMN_START_DATE, TimerForegroundService.startDate);
        values.put(HeadiDBContract.Diary.COLUMN_END_DATE, TimerForegroundService.endDate);
        values.put(HeadiDBContract.Diary.COLUMN_DURATION, TimerForegroundService.elapsedTime);
        values.put(HeadiDBContract.Diary.COLUMN_REGION, getDrawableAsByteArray(region));
        values.put(HeadiDBContract.Diary.COLUMN_DESCRIPTION, description);
        values.put(HeadiDBContract.Diary.COLUMN_MEDICATION, medication);
        values.put(HeadiDBContract.Diary.COLUMN_MEDICATION_AMOUNT, medicationAmount);
        values.put(HeadiDBContract.Diary.COLUMN_STRENGTH, strength);
        String pain = ((Cursor) pains_items.getSelectedItem()).getString(1);
        values.put(HeadiDBContract.Diary.COLUMN_PAIN, pain);

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

        // set the save layout
        final View saveView = getLayoutInflater().inflate(R.layout.fragment_timer_dialog, null);
        builder.setView(saveView);

        // set up finger paint view
        FingerPaintImageView finger = saveView.findViewById(R.id.diary_region);
        finger.setStrokeColor(requireActivity().getColor(R.color.region_marking));
        finger.setStrokeWidth(20);
        finger.setTouchTolerance(1);

        TextView button_undo = saveView.findViewById(R.id.button_undo);
        TextView button_clear = saveView.findViewById(R.id.button_clear);

        button_undo.setOnClickListener(v -> finger.undo());
        button_clear.setOnClickListener(v -> finger.clear());

        SeekBar strength = saveView.findViewById(R.id.diary_strength);

        // add save button
        builder.setPositiveButton(context.getString(R.string.save_button), (dialog, which) -> {
            EditText diaryDescription = saveView.findViewById(R.id.diary_description);

            Spinner medication = saveView.findViewById(R.id.diary_medication);
            String diaryMedication = ((Cursor) medication.getSelectedItem()).getString(1);

            TextView diary_medication_amount = saveView.findViewById(R.id.diary_medication_amount);
            int diaryMedicationAmount = Integer.parseInt(diary_medication_amount.getText().toString());

            if (diaryMedicationAmount == 0) {
                diaryMedication = "";
            }

            saveToDB(finger.getDrawable(), diaryDescription.getText().toString(), diaryMedication,
                    strength.getProgress(), diaryMedicationAmount);
        });

        // add delete button
        builder.setNegativeButton(context.getString(R.string.cancel_button), (dialog, which) -> {

        });

        // add cancel button
        builder.setNeutralButton(context.getString(R.string.delete_button),
                (dialog, which) -> timerForegroundServiceEndAction());

        // populate medication spinner
        populateMedicationSpinner(saveView);

        // increase and decrease medication amount buttons
        ImageView button_increase = saveView.findViewById(R.id.button_increase);
        ImageView button_decrease = saveView.findViewById(R.id.button_decrease);
        button_increase.setOnClickListener(v -> increaseMedicationAmount(saveView));
        button_decrease.setOnClickListener(v -> decreaseMedicationAmount(saveView));

        // Set pain strength text
        TextView pain_strength_text = saveView.findViewById(R.id.diary_strength_text);
        pain_strength_text.setText(context.getString(R.string.strength_of_10, Integer.toString(strength.getProgress())));

        strength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                pain_strength_text.setText(context.getString(R.string.strength_of_10, Integer.toString(strength.getProgress())));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void increaseMedicationAmount(View view) {
        TextView medication_amount = view.findViewById(R.id.diary_medication_amount);
        int newVal = Integer.parseInt(medication_amount.getText().toString()) + 1;
        medication_amount.setText(String.format(Locale.getDefault(), "%d", newVal));
    }

    private void decreaseMedicationAmount(View view) {
        TextView medication_amount = view.findViewById(R.id.diary_medication_amount);
        int newVal = Integer.parseInt(medication_amount.getText().toString()) - 1;
        medication_amount.setText(newVal < 0 ? "0" : Integer.toString(newVal));
    }

    private void populateMedicationSpinner(View view) {
        Context context = requireActivity();
        Spinner medication_items = view.findViewById(R.id.diary_medication);

        // Attach cursor adapter to the ListView
        HeadiDBSQLiteHelper helper = new HeadiDBSQLiteHelper(context);
        MedicationsCourserAdapter adapter = helper.readMedicationsWithoutIconFromDB(context);
        medication_items.setAdapter(adapter);
    }

    private void setSpinnerPain(PainsCourserIconAdapter adapter) {
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