package com.headi.app;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_timer, R.id.nav_pains, R.id.nav_diary,
                R.id.nav_medication, R.id.nav_stats, R.id.nav_backup_restore,
                R.id.nav_about)
                .setOpenableLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Request Notification Permission
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            String[] perm = new String[]{Manifest.permission.POST_NOTIFICATIONS};
            ActivityCompat.requestPermissions(MainActivity.this, perm, 0);
        }
        // Create notification channel
        createNotificationChannel();

    } catch (Exception e) {
        Log.e("Error scheisse", "onCreateView", e);
        throw e;
    }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel
        CharSequence name = "Timer";
        String description = "Display of the time of the running timer";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(Constants.SERVICE.NOTIFICATION_CHANEL_ID, name,
                importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}