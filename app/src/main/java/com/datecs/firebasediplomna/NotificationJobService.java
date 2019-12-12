package com.datecs.firebasediplomna;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

/**
 * The Service that JobScheduler runs once the conditions are met.
 * In this case it posts a notification.
 */
public class NotificationJobService extends JobService {

    /**
     * This is called by the system once it determines it is time to run the job.
     * @param jobParameters Contains the information about the job
     * @return Boolean indicating whether or not the job was offloaded to a separate thread.
     * In this case, it is false since the notification can be posted on the main thread.
     */
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        //Set up the notification content intent to launch the app when clicked
        PendingIntent contentPendingIntent = PendingIntent.getActivity
                (this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //For Androids after Oreo is needed a second channel
        if (Build.VERSION.SDK_INT >= 26) {
            String id = "channel_1";
            String description = "143";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(id, description, importance);
            channel.enableLights(true);
            channel.enableVibration(true);
            manager.createNotificationChannel(channel);
            Notification notification = new Notification.Builder(this, id)
                    .setCategory(Notification.CATEGORY_MESSAGE)
                    .setSmallIcon(R.drawable.ic_warning_black_24dp)
                    .setContentTitle("Warning")
                    .setContentText("Bigger value than expected is measured")
                    .setContentIntent(contentPendingIntent)
                    .setAutoCancel(true)
                    .build();
            manager.notify(1, notification);
        } else {
            //When sdk version is less than 26
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("Warning")
                    .setContentText("Bigger value than expected is measured")
                    .setContentIntent(contentPendingIntent)
                    .setSmallIcon(R.drawable.ic_warning_black_24dp)
                    .build();
            manager.notify(1, notification);
        }

        return true;
    }

    /**
     * Called by the system when the job is running but the conditions are no longer met.
     * In this example it is never called since the job is not offloaded to a different thread.
     *
     * @param jobParameters Contains the information about the job
     * @return Boolean indicating whether the job needs rescheduling
     */
    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }
}