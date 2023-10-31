package com.cleartwo.tvapplication;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.SystemClock;

/* loaded from: classes.dex */
public class TimeOutReceiver extends BroadcastReceiver {
    @SuppressLint("UnspecifiedImmutableFlag")
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences("autostart", 0);
        String packageNames = "com.cleartwo.tvapplication";
        String classNames = "MainActivity.class";
//        String packageNames = prefs.getString("package", "");
//        String classNames = prefs.getString("class", "");
        int timeout = prefs.getInt("inbetweendelay", 0);
        boolean enabled = true;
        boolean notificaton = prefs.getBoolean("noti", true);
        int pos = prefs.getInt("iteration", 0);
        if (enabled && !packageNames.equalsIgnoreCase("") && !classNames.equalsIgnoreCase("")) {
            try {
                String[] splitPackages = packageNames.split(";;");
                String[] splitClassNames = classNames.split(";;");
                if (splitClassNames.length > pos && splitPackages.length > pos) {
                    String packageName = splitPackages[pos];
                    String className = splitClassNames[pos];
//                    if (notificaton) {
//                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//                        CharSequence contentText = "Started application: " + className;
//                        Intent notificationIntent = new Intent(context, MainActivity.class);
//                        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
//                        Notification mNotification = new Notification(R.drawable.iv_notification_image, contentText, System.currentTimeMillis());
////                        mNotification.setLatestEventInfo(context, "Auto Start", contentText, contentIntent);
//                        mNotificationManager.notify(3461, mNotification);
//                    }
                    PackageManager pm = context.getPackageManager();
                    Intent i = pm.getLaunchIntentForPackage(packageName);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                    int pos2 = pos + 1;
                    SharedPreferences.Editor editor = prefs.edit();
                    if (pos2 == splitPackages.length) {
                        editor.putInt("iteration", 0);
                    } else {
                        editor.putInt("iteration", pos2);
                        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        Intent ii = new Intent(context, TimeOutHomeReceiver.class);
//                        PendingIntent pi = PendingIntent.getBroadcast(context, 0, ii, 0);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                            pendingIntent = PendingIntent.getBroadcast(
                                    context,
                                    0, ii,
                                    PendingIntent.FLAG_IMMUTABLE);
                        }
                        else
                        {
                            pendingIntent = PendingIntent.getBroadcast(
                                    context,
                                    0, ii,
                                    PendingIntent.FLAG_UPDATE_CURRENT);
                        }
                        mgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + (timeout * 1000), pendingIntent);
                    }
                    editor.commit();
                }
            } catch (Exception e) {
            }
        }
    }
}
