package com.cleartwo.tvapplication;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

/* loaded from: classes.dex */
public class TimeOutHomeReceiver extends BroadcastReceiver {
    @SuppressLint("UnspecifiedImmutableFlag")
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        try {
            SharedPreferences prefs = context.getSharedPreferences("autostart", 0);
            boolean gotoHome = prefs.getBoolean("gotohome", true);
            if (gotoHome) {
                try {
                    Intent startMain = new Intent("android.intent.action.MAIN");
                    startMain.addCategory("android.intent.category.HOME");
                    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(startMain);
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
            AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent i = new Intent(context, TimeOutReceiver.class);
//            PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                pendingIntent = PendingIntent.getBroadcast(
                        context,
                        0, i,
                        PendingIntent.FLAG_IMMUTABLE);
            }
            else
            {
                pendingIntent = PendingIntent.getBroadcast(
                        context,
                        0, i,
                        PendingIntent.FLAG_UPDATE_CURRENT);
            }
            mgr.set(AlarmManager.RTC, SystemClock.elapsedRealtime() + 1000, pendingIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
