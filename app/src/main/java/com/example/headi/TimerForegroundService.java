package com.example.headi;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

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

        stopwatch = new StopwatchBuilder()
                // Set the initial format
                .startFormat("HH:MM:SS")
                // Set the tick listener for displaying time
                .onTick(this::updateNotification)
                // When time is equal to one hour, change format to "HH:MM:SS"
                .changeFormatWhen(1, TimeUnit.HOURS, "HH:MM:SS")
                .build();

        calcTimes();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        switch (intent.getAction()) {
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

    private Notification prepareNotification(CharSequence time) {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        return new NotificationCompat.Builder(this, Constants.SERVICE.NOTIFICATION_CHANEL_ID)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(time)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOnlyAlertOnce(true) // so when data is updated don't make sound and alert in android 8.0+
                .setOngoing(true)
                .build();
    }

    private void updateNotification(CharSequence time) {
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
        elapsedTime = stopwatch.getTimeIn(TimeUnit.MILLISECONDS);
        startDate = System.currentTimeMillis() - elapsedTime;
        endDate = System.currentTimeMillis();
    }
}

