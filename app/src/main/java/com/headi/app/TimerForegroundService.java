package com.headi.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import timerx.Stopwatch;
import timerx.StopwatchBuilder;

public class TimerForegroundService extends Service {

    public Stopwatch stopwatch;
    public static boolean isTimerRunning = false;
    public static CharSequence currentTime = "00:00:00";

    public static Long startDate = 0L;
    public static Long endDate = 0L;
    public static Long elapsedTime = 0L;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        isTimerRunning = false;
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        StopwatchBuilder builder = new StopwatchBuilder();
        // Set the initial format
        builder.startFormat("HH:MM:SS");
        // Set the tick listener for displaying time
        builder.onTick(this::updateNotification);
        // When time is equal to one hour, change format to "HH:MM:SS"
        builder.changeFormatWhen(1, TimeUnit.HOURS, "HH:MM:SS");
        stopwatch = builder.build$timerx_core_release();

        calcTimes();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        switch (Objects.requireNonNull(intent.getAction())) {
            case Constants.ACTION.START_ACTION:
                startForeground(Constants.SERVICE.NOTIFICATION_ID_TIMER_SERVICE, prepareNotification("00:00:00"));
                stopwatch.start();
                isTimerRunning = true;
                break;
            case Constants.ACTION.STOP_ACTION:
                stopwatch.stop();
                isTimerRunning = false;
                break;
            case Constants.ACTION.END_ACTION:
                stopForeground(true);
                stopwatch.release();
                isTimerRunning = false;
                currentTime = "00:00:00";
                elapsedTime = 0L;
                stopSelf();
                break;
            case Constants.ACTION.NONE_ACTION:
                break;
            default:
                stopForeground(true);
                isTimerRunning = false;
                stopwatch.release();
                stopSelf();
        }

        return START_NOT_STICKY;
    }

    private Bitmap getLargeIconBitmap() {
        VectorDrawableCompat vectorDrawable = VectorDrawableCompat.create(getResources(), R.drawable.ic_play_icon, null);
        assert vectorDrawable != null;
        vectorDrawable.setTint(ContextCompat.getColor(this, R.color.button_play));

        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);

        return bitmap;
    }

    private Notification prepareNotification(CharSequence time) {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, Constants.SERVICE.NOTIFICATION_CHANEL_ID)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(time)
                .setLargeIcon(getLargeIconBitmap())
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setColor(getColor(R.color.primaryColor))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOnlyAlertOnce(true) // so when data is updated don't make sound and alert in android 8.0+
                .setOngoing(true)
                .build();
    }

    private void updateNotification(long mills, CharSequence time) {
        currentTime = time;

        Notification notification = prepareNotification(time);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(Constants.SERVICE.NOTIFICATION_ID_TIMER_SERVICE, notification);

        Intent timerIntent = new Intent();
        timerIntent.putExtra(Constants.BROADCAST.DATA_CURRENT_TIME, time);
        timerIntent.setAction(Constants.BROADCAST.ACTION_CURRENT_TIME);
        sendBroadcast(timerIntent);

        calcTimes();
    }

    private void calcTimes() {
        elapsedTime = stopwatch.getCurrentTimeInMillis();
        startDate = System.currentTimeMillis() - elapsedTime;
        endDate = System.currentTimeMillis();
    }
}

