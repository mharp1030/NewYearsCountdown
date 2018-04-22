package com.moeharp.newyearscountdown;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends Service
{
    private Timer timer;
    private TimerTask timerTask;
    private int totalSeconds, intervals, totalIntervals = 0;
    private String msg;
    final Handler handler = new Handler();

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flag, int ID)
    {
        super.onStartCommand(intent, flag, ID);

        // Get data from shared preferences and store them in variables for faster access
        SharedPreferences prefs = getSharedPreferences("timer", MODE_PRIVATE);
        totalSeconds = prefs.getInt("countdown", 0);
        intervals = prefs.getInt("Interval", 0);
        msg = prefs.getString("message", "");

        startTimer();

        return START_STICKY;
    }

    @Override
    public void onCreate() {}

    @Override
    public void onDestroy()
    {
        stopTimer();
        super.onDestroy();
    }

    private  void startTimer()
    {
        // Set a new Timer
        timer = new Timer();

        initializeTimer();

        // Timer will not notify user until first interval passes
        timer.schedule(timerTask, intervals * 1000, intervals * 1000);
    }

    private void stopTimer()
    {
        if (timer != null)
        {
            timer.cancel();
            timer = null;
        }
    }

    private void initializeTimer()
    {
        timerTask = new TimerTask()
        {
            public void run()
            {
                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable()
                {
                    public void run()
                    {
                        manageTimer();
                    }
                });
            }
        };
    }

    private void manageTimer()
    {
        // Keep track of intervals
        totalIntervals += intervals;

        // if current total intervals is >= the specified total seconds, send final notification
        // and destroy service, else send the countdown notification
        if(totalIntervals >= totalSeconds)
        {
            sendNotification("Time for: " + msg + "!");
            onDestroy();
        }
        else
        {
            sendNotification((totalSeconds - totalIntervals) + " Seconds Until " + msg +"!");

            /*
            *   if the difference of total seconds and total intervals is less than intervals
            *   then the intervals need to be reset to the remaining seconds left, so that the timer
            *   doesn't count past it's allowed limit. For example, 35 seconds with 30 second
            *   intervals, would run for 60 seconds without this if statement to set it for 30
            *   seconds, then update intervals for remaining 5 seconds
            */
            if((totalSeconds - totalIntervals) < intervals)
            {
                intervals = (totalSeconds - totalIntervals);
                stopTimer();
                startTimer(); // start timer again, with updated interval settings
            }
        }
    }

    private void sendNotification(String text)
    {
        // create the intent for the notification
        Intent notificationIntent = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // create the pending intent
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, flags);

        // create the variables for the notification
        int icon = R.drawable.ic_launcher_background;
        CharSequence contentTitle = getText(R.string.app_name);

        // display the notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Displays notification for Android 8 devices and newer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            // create the notification and set its data
            Notification notification = new Notification.Builder(this, "Count1")
                    .setSmallIcon(icon)
                    .setTicker(text)
                    .setContentTitle(contentTitle)
                    .setContentText(text)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();

            // create a notification channel
            NotificationChannel channel = new NotificationChannel("Count1",
                    "Countdown", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);

            manager.notify(0, notification);
        }
        else // Displays for android 7 and older
        {
            // create the notification and set its data
            Notification notification = new Notification.Builder(this)
                    .setSmallIcon(icon)
                    .setTicker(text)
                    .setContentTitle(contentTitle)
                    .setContentText(text)
                    .setContentIntent(pendingIntent)
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .build();

            manager.notify(0, notification);
        }
    }
}
