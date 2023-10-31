package com.cleartwo.tvapplication;

import static com.cleartwo.tvapplication.utils.APIsCall.clear_cache;
import static com.cleartwo.tvapplication.utils.APIsCall.schedule;
import static com.cleartwo.tvapplication.utils.APIsCall.updateStatus;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.cleartwo.tvapplication.utils.SharedPrefHelper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

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
            Log.e("refetch_schedule", remoteMessage.getNotification().getTitle());

            if (remoteMessage.getNotification().getBody() != null) {
                if (remoteMessage.getNotification().getBody().equals("refetch_schedule")) {
                    Const.mainActivity.currentplaylist = "";
                    Const.mainActivity.order = 1;
                    schedule(Const.mainActivity.apiInterface, Const.mainActivity);
                } else if (remoteMessage.getNotification().getBody().equals("restart_app")) {

                    Timer timer = new Timer();
                    Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
                    calendar.set(Calendar.SECOND, 0);
                    calendar.add(Calendar.MINUTE, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    Date date = calendar.getTime();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            triggerRebirth(Const.mainActivity);
                        }
                    }, date);

                } else if (remoteMessage.getNotification().getBody().equals("shutdown_app")) {
                    triggerFinish(Const.mainActivity);
                } else if (remoteMessage.getNotification().getBody().equals("restart_unit")) {
                    try {
                        Runtime.getRuntime().exec("reboot");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                } else if (remoteMessage.getNotification().getBody().equals("update_app")) {
                    Log.e("remoteMessage.getNotification().getBody()", remoteMessage.getNotification().getBody());
                } else if (remoteMessage.getNotification().getBody().equals("report_status")) {
                    updateStatus(Const.mainActivity.apiInterface);
                } else if (remoteMessage.getNotification().getBody().equals("clear_cache")) {
                    clear_cache = true;
                    Const.mainActivity.currentplaylist = "";
                    Const.mainActivity.order = 1;
                    schedule(Const.mainActivity.apiInterface, Const.mainActivity);
                } else if (remoteMessage.getNotification().getBody().equals("logout")) {
                    SharedPrefHelper.saveData(Const.mainActivity, "unit_id", "");
                    Const.mainActivity.initView();
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
        Const.mainActivity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(Const.mainActivity, "App Restarted", Toast.LENGTH_SHORT).show();
            }
        });
        Const.mainActivity.currentplaylist = "";
        Const.mainActivity.order = 1;
        Const.mainActivity.timerSchedule();
    }

    public static void triggerFinish(Context context) {
        Const.mainActivity.finish();
    }

}