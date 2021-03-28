package com.headi.app.ui.timer;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.headi.app.Constants;
import com.headi.app.R;
import com.headi.app.TimerForegroundService;
import com.headi.app.db.DiaryStats;
import com.headi.app.db.HeadiDBContract;
import com.headi.app.db.HeadiDBSQLiteHelper;
import com.headi.app.db.MedicationsCourserAdapter;
import com.headi.app.db.PainsCourserIconAdapter;
import com.headi.app.ui.UiHelper;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

import tech.picnic.fingerpaintview.FingerPaintImageView;


public class TimerFragment extends Fragment {

    private View view;
    private Spinner pains_items;
    private Button button_start;
    private ImageButton button_save;
    private ImageButton button_delete;
    private LineChart lineDurationOverTime;

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
        } else {
            bitmap = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
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
        button_save = view.findViewById(R.id.timer_save_button);
        button_delete = view.findViewById(R.id.timer_delete_button);
        pains_items = view.findViewById(R.id.timer_pains_select);

        setupCharts();
        readFromStatsDB();
        registerListeners();
        readFromDB();
        setUiAppearance(Constants.ACTION.INIT_ACTION);

        return view;
    }

    private void setupCharts() {
        // Line Chart: Pain minutes over time
        lineDurationOverTime = view.findViewById(R.id.stats_duration_over_time);
        lineDurationOverTime.getDescription().setEnabled(false);
        lineDurationOverTime.setScaleEnabled(false);
        lineDurationOverTime.setDragEnabled(false);
        lineDurationOverTime.setTouchEnabled(false);
        lineDurationOverTime.setPinchZoom(false);
        lineDurationOverTime.getLegend().setEnabled(false);
        lineDurationOverTime.setHighlightPerTapEnabled(false);
        lineDurationOverTime.getXAxis().setValueFormatter(new DiaryStats.LineChartXAxisValueFormatter());

        XAxis xAxis = lineDurationOverTime.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(UiHelper.getPrimaryTextColor(getActivity()));
        xAxis.setLabelCount(2, true);

        YAxis yAxis = lineDurationOverTime.getAxisLeft();
        yAxis.setDrawGridLines(false);
        yAxis.setTextColor(UiHelper.getPrimaryTextColor(getActivity()));
        yAxis.setAxisMinimum(0f);
        lineDurationOverTime.getAxisRight().setDrawLabels(false);
    }

    private void readFromStatsDB() {
        Context context = getActivity();
        TextView timer_graph_description = view.findViewById(R.id.timer_graph_description);
        ImageView timer_stats_trend_icon = view.findViewById(R.id.timer_stats_trend_icon);

        // Time filter
        String selection = HeadiDBContract.Diary.COLUMN_START_DATE + " >= ? AND " + HeadiDBContract.Diary.COLUMN_END_DATE + " <= ?";
        String to = Long.toString(System.currentTimeMillis());
        String from = Long.toString(System.currentTimeMillis() - 1209600000L); // 14 days
        String[] selectionArgs = {from, to};

        HeadiDBSQLiteHelper helper = new HeadiDBSQLiteHelper(context);
        DiaryStats diaryStats = helper.readDiaryStatsFromDB(context, selection, selectionArgs);

        lineDurationOverTime.setData(diaryStats.getDurationOverTime(true, false));
        lineDurationOverTime.invalidate();

        // set trend icon
        ImageViewCompat.setImageTintList(timer_stats_trend_icon, ColorStateList.valueOf(Constants.MATERIAL_COLORS_500[2]));
        if (diaryStats.trendSlope > 0) {
            timer_stats_trend_icon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_trend_up, null));
            ImageViewCompat.setImageTintList(timer_stats_trend_icon, ColorStateList.valueOf(Constants.MATERIAL_COLORS_500[0]));
        }
        else if (diaryStats.trendSlope < 0) {
            timer_stats_trend_icon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_trend_down, null));
            ImageViewCompat.setImageTintList(timer_stats_trend_icon, ColorStateList.valueOf(Constants.MATERIAL_COLORS_500[5]));
        }
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
            // check if at least one pain is available
            if (onePainAvailable()) {
                requireActivity().startService(intent);
                setUiAppearance(intent.getAction());
            }
        });

        // Save Button listener
        button_save.setOnClickListener(v -> openSaveDialog());

        // Delete Button listener
        button_delete.setOnClickListener(v -> timerForegroundServiceEndAction());

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

    private boolean onePainAvailable() {
        Context context = getActivity();
        boolean pain_available = pains_items.getAdapter().getCount() > 0;

        if (!pain_available) {
            // Create an alert builder
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.title_no_pain_available));
            builder.setMessage(context.getString(R.string.ask_for_new_pain));

            builder.setPositiveButton(context.getString(R.string.button_ok), (dialog, which) -> dialog.dismiss());

            // create and show the alert dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        return pain_available;
    }

    private boolean oneMedicationAvailable(View view) {
        Spinner medication = view.findViewById(R.id.diary_medication);
        return medication.getAdapter().getCount() > 0;
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

    private void setButton(Button button, String action) {
        switch (action) {
            case Constants.ACTION.STOP_ACTION:
                button.setText(requireActivity().getString(R.string.timer_start));
                button.setBackgroundColor(requireActivity().getColor(R.color.button_play));
                break;
            case Constants.ACTION.START_ACTION:
                button.setText(requireActivity().getString(R.string.timer_stop));
                button.setBackgroundColor(requireActivity().getColor(R.color.button_stop));
                button_save.setEnabled(false);
                button_delete.setEnabled(false);
                break;
            default:
                break;
        }
        setSaveAndDeleteButton(action);
    }

    private void setSaveAndDeleteButton(String action) {
        switch (action) {
            case Constants.ACTION.STOP_ACTION:
                if (TimerForegroundService.elapsedTime == 0L) {
                    button_save.setEnabled(false);
                    button_delete.setEnabled(false);
                    button_save.setImageAlpha(0x3F);
                    button_delete.setImageAlpha(0x3F);
                }
                else {
                    button_save.setEnabled(true);
                    button_delete.setEnabled(true);
                    button_save.setImageAlpha(0xFF);
                    button_delete.setImageAlpha(0xFF);
                }
                break;
            case Constants.ACTION.START_ACTION:
            case Constants.ACTION.END_ACTION:
                button_save.setEnabled(false);
                button_delete.setEnabled(false);
                button_save.setImageAlpha(0x3F);
                button_delete.setImageAlpha(0x3F);
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
        setSaveAndDeleteButton(Constants.ACTION.END_ACTION);
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

            String diaryMedication = "";
            if (oneMedicationAvailable(saveView)) {
                Spinner medication = saveView.findViewById(R.id.diary_medication);
                diaryMedication = ((Cursor) medication.getSelectedItem()).getString(1);
            }

            TextView diary_medication_amount = saveView.findViewById(R.id.diary_medication_amount);
            int diaryMedicationAmount = Integer.parseInt(diary_medication_amount.getText().toString());

            if (diaryMedicationAmount == 0) {
                diaryMedication = "";
            }

            saveToDB(finger.getDrawable(), diaryDescription.getText().toString(), diaryMedication,
                    strength.getProgress(), diaryMedicationAmount);
        });

        // add cancel button
        builder.setNegativeButton(context.getString(R.string.cancel_button), (dialog, which) -> dialog.dismiss());

        // add delete button
        builder.setNeutralButton(context.getString(R.string.delete_button), (dialog, which) -> timerForegroundServiceEndAction());

        // populate medication spinner
        populateMedicationSpinner(saveView);

        // increase and decrease medication amount buttons
        ImageView button_increase = saveView.findViewById(R.id.button_increase);
        ImageView button_decrease = saveView.findViewById(R.id.button_decrease);
        button_increase.setOnClickListener(v -> increaseMedicationAmount(saveView));
        button_decrease.setOnClickListener(v -> decreaseMedicationAmount(saveView));
        if (!oneMedicationAvailable(saveView)) {
            button_increase.setEnabled(false);
            button_decrease.setEnabled(false);
        }

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