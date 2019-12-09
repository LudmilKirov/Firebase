package com.datecs.firebasediplomna;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;

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

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("Bepche")
                .setContentText("Zdr")
                .setContentIntent(contentPendingIntent)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_light)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true);

        manager.notify(0, builder.build());

        return false;
    }

    /**
     * Called by the system when the job is running but the conditions are no longer met.
     * In this example it is never called since the job is not offloaded to a different thread.
     * @param jobParameters Contains the information about the job
     * @return Boolean indicating whether the job needs rescheduling
     */
    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }
}