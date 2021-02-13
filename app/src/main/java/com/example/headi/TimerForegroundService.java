package com.example.headi;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.headi.MainActivity;
import com.example.headi.R;
import com.example.headi.Constants;

import java.util.concurrent.TimeUnit;

import timerx.Stopwatch;
import timerx.StopwatchBuilder;

public class TimerForegroundService extends Service {

    public Stopwatch stopwatch;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        stopwatch = new StopwatchBuilder()
                // Set the initial format
                .startFormat("HH:MM:SS")
                // Set the tick listener for displaying time
                .onTick(time -> updateNotification(time))
                // When time is equal to one hour, change format to "HH:MM:SS"
                .changeFormatWhen(1, TimeUnit.HOURS, "HH:MM:SS")
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        switch (intent.getAction()) {
            case Constants.ACTION.START_ACTION:
                startForeground(Constants.SERVICE.NOTIFICATION_ID_TIMER_SERVICE, prepareNotification("00:00:00"));
                stopwatch.start();
                break;
            case Constants.ACTION.STOP_ACTION:
                stopForeground(true);
                stopwatch.stop();
                break;
            case Constants.ACTION.SAVE_ACTION:
                stopForeground(true);
                stopwatch.stop();
                stopSelf();
                break;
            default:
                stopForeground(true);
                stopSelf();
        }

        return START_NOT_STICKY;

    }

    private Notification prepareNotification(CharSequence time) {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, Constants.SERVICE.NOTIFICATION_CHANEL_ID)
                .setContentTitle("getText(R.string.notification_title)")
                .setContentText(time)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOnlyAlertOnce(true) // so when data is updated don't make sound and alert in android 8.0+
                .setOngoing(true)
                .build();

        return notification;
    }

    private void updateNotification(CharSequence time) {
        Notification notification = prepareNotification(time);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(Constants.SERVICE.NOTIFICATION_ID_TIMER_SERVICE, notification);
    }
}

