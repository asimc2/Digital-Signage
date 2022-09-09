package com.cleartwo.tvapplication;

import static com.cleartwo.tvapplication.utils.APIsCall.schedule;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class FirebaseMessageReceiver
        extends FirebaseMessagingService {

    // Override onMessageReceived() method to extract the
    // title and
    // body from the message passed in FCM
    @SuppressLint("LongLogTag")
    @Override
    public void
    onMessageReceived(RemoteMessage remoteMessage) {

        // Second case when notification payload is
        // received.
        if (remoteMessage.getNotification() != null) {
//            Const.mainActivity.methodInit("android.resource://" + getPackageName() + "/" + R.raw.archies1);
//            Const.mainActivity.checkSchedule();
            Log.e("refetch_schedule", remoteMessage.getNotification().getTitle());

            if (remoteMessage.getNotification().getBody() != null) {
                if (remoteMessage.getNotification().getBody().equals("refetch_schedule")) {
                    Const.mainActivity.currentplaylist = "";
                    Const.mainActivity.order = 1;
                    schedule(Const.mainActivity.apiInterface, Const.mainActivity);
                } else if (remoteMessage.getNotification().getBody().equals("restart_app")) {
                    triggerRebirth(Const.mainActivity);
                } else if (remoteMessage.getNotification().getBody().equals("shutdown_app")) {
                    triggerFinish(Const.mainActivity);
                } else if (remoteMessage.getNotification().getBody().equals("update_app")) {
                    Log.d("TAG", "From: " + remoteMessage.getFrom());
                }
            }
            // Since the notification is received directly from
            // FCM, the title and the body can be fetched
            // directly as below.
            Log.e("remoteMessage.getNotification().getTitle()", remoteMessage.getNotification().getTitle());
            Log.e("remoteMessage.getNotification().getBody()", remoteMessage.getNotification().getBody());
        }
    }

    public static void triggerRebirth(Context context) {
        Const.mainActivity.currentplaylist = "";
        Const.mainActivity.order = 1;
        Const.mainActivity.timerSchedule();
    }

    public static void triggerFinish(Context context) {
        Const.mainActivity.finish();
    }

    // Method to get the custom Design for the display of
    // notification.
    private RemoteViews getCustomDesign(String title,
                                        String message) {
        RemoteViews remoteViews = new RemoteViews(
                getApplicationContext().getPackageName(),
                R.layout.notification);
        remoteViews.setTextViewText(R.id.title, title);
        remoteViews.setTextViewText(R.id.message, message);
        remoteViews.setImageViewResource(R.id.icon,
                R.mipmap.ic_launcher);
        return remoteViews;
    }

    // Method to display the notifications
    public void showNotification(String title,
                                 String message) {
        // Pass the intent to switch to the MainActivity
        Intent intent
                = new Intent(this, MainActivity.class);
        // Assign channel ID
        String channel_id = "notification_channel";
        // Here FLAG_ACTIVITY_CLEAR_TOP flag is set to clear
        // the activities present in the activity stack,
        // on the top of the Activity that is to be launched
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Pass the intent to PendingIntent to start the
        // next Activity
        PendingIntent pendingIntent
                = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        // Create a Builder object using NotificationCompat
        // class. This will allow control over all the flags
        NotificationCompat.Builder builder
                = new NotificationCompat
                .Builder(getApplicationContext(),
                channel_id)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setVibrate(new long[]{1000, 1000, 1000,
                        1000, 1000})
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent);

        // condition for the same is checked here.
        if (Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.JELLY_BEAN) {
            builder = builder.setContent(
                    getCustomDesign(title, message));
        } else {
            builder = builder.setContentTitle(title)
                    .setContentText(message)
                    .setSmallIcon(R.mipmap.ic_launcher);
        }
        // Create an object of NotificationManager class to
        // notify the
        // user of events that happen in the background.
        NotificationManager notificationManager
                = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE);
        // Check if the Android Version is greater than Oreo
        if (Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel
                    = new NotificationChannel(
                    channel_id, "web_app",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(
                    notificationChannel);
        }

        notificationManager.notify(0, builder.build());
    }
}