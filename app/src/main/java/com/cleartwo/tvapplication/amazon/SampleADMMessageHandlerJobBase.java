package com.cleartwo.tvapplication.amazon;

import static com.cleartwo.tvapplication.FirebaseMessageReceiver.triggerFinish;
import static com.cleartwo.tvapplication.FirebaseMessageReceiver.triggerRebirth;
import static com.cleartwo.tvapplication.utils.APIsCall.clear_cache;
import static com.cleartwo.tvapplication.utils.APIsCall.schedule;
import static com.cleartwo.tvapplication.utils.APIsCall.updateStatus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.amazon.device.messaging.ADMConstants;
import com.amazon.device.messaging.ADMMessageHandlerJobBase;
import com.cleartwo.tvapplication.Const;
import com.cleartwo.tvapplication.R;
import com.cleartwo.tvapplication.utils.SharedPrefHelper;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The SampleADMMessageHandlerJobBase class receives messages sent by ADM via the SampleADMMessageReceiver receiver.
 *
 * @version Revision: 1, Date: 11/20/2019
 */

public class SampleADMMessageHandlerJobBase extends ADMMessageHandlerJobBase {
    /**
     * Tag for logs.
     */
    private final static String TAG = "ADMSampleJobBase";

    /**
     * Class constructor.
     */
    public SampleADMMessageHandlerJobBase() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressLint({"LongLogTag", "SimpleDateFormat"})
    @Override
    protected void onMessage(final Context context, final Intent intent) {
        Log.i(TAG, "SampleADMMessageHandlerJobBase:onMessage");
//        Const.mainActivity.finish();
        /* String to access message field from data JSON. */
        final String msgKey = context.getString(R.string.json_data_msg_key);

        /* String to access timeStamp field from data JSON. */
        final String timeKey = context.getString(R.string.json_data_time_key);

        /* Intent action that will be triggered in onMessage() callback. */
        final String intentAction = context.getString(R.string.intent_msg_action);

        /* Extras that were included in the intent. */
        final Bundle extras = intent.getExtras();

        verifyMD5Checksum(context, extras);

        /* Extract message from the extras in the intent. */
        final String msg = extras.getString(msgKey);
        final String time = extras.getString("123");

        if (msg == null || time == null) {
            Log.w(TAG, "SampleADMMessageHandlerJobBase:onMessage Unable to extract message data." +
                    "Make sure that msgKey and timeKey values match data elements of your JSON message");
        }

        /* Create a notification with message data. */
        /* This is required to test cases where the app or device may be off. */
        ADMHelper.createADMNotification(context, msgKey, timeKey, intentAction, msg, time);

        if (msg != null) {
//            Const.mainActivity.methodInit("android.resource://" + getPackageName() + "/" + R.raw.archies1);
//            Const.mainActivity.checkSchedule();
            Log.e("refetch_schedule", msg);
            if (msg.equals("refetch_schedule")) {
                Const.mainActivity.currentplaylist = "";
                Const.mainActivity.order = 1;
                schedule(Const.mainActivity.apiInterface, Const.mainActivity);
            } else if (msg.equals("restart_app")) {
                boolean i = true;

                Timer timer = new Timer();
                Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
                calendar.set(Calendar.SECOND, 0);
                calendar.add(Calendar.MINUTE, 1);
                calendar.set(Calendar.MILLISECOND, 0);
                Date date = calendar.getTime();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        triggerRebirth(Const.mainActivity);
                    }
                }, date);

//                while (i) {
//
//                    int seconds = Calendar.getInstance().get(Calendar.SECOND);
//                        if (seconds == 0 || seconds == 10 || seconds == 20 || seconds == 30 || seconds == 40 || seconds == 50) {
//                            triggerRebirth(Const.mainActivity);
//                            i = false;
//                        }
//                }

            } else if (msg.equals("shutdown_app")) {
                triggerFinish(Const.mainActivity);
            } else if (msg.equals("update_app")) {
//                Log.d("TAG", "From: " + remoteMessage.getFrom());
//                startActivity(new Intent(this, MainActivity.class));
            } else if (msg.equals("report_status")) {
                updateStatus(Const.mainActivity.apiInterface);
            } else if (msg.equals("clear_cache")) {
                clear_cache = true;
                Const.mainActivity.currentplaylist = "";
                Const.mainActivity.order = 1;
                schedule(Const.mainActivity.apiInterface, Const.mainActivity);
            } else if (msg.equals("logout")) {
                SharedPrefHelper.saveData(context, "unit_id", "");
                Const.mainActivity.initView();
            }

            // Since the notification is received directly from
            // FCM, the title and the body can be fetched
            // directly as below.
            Log.e("remoteMessage.getNotification().getTitle()", msg);
            Log.e("remoteMessage.getNotification().getBody()", msg);
//            showNotification(
//                    remoteMessage.getNotification().getTitle(),
//                    remoteMessage.getNotification().getBody());
        }
    }

    /**
     * This method verifies the MD5 checksum of the ADM message.
     *
     * @param extras Extra that was included with the intent.
     */
    private void verifyMD5Checksum(final Context context, final Bundle extras) {
        /* String to access consolidation key field from data JSON. */
        final String consolidationKey = context.getString(R.string.json_data_consolidation_key);

        final Set<String> extrasKeySet = extras.keySet();
        final Map<String, String> extrasHashMap = new HashMap<String, String>();
        for (String key : extrasKeySet) {
            if (!key.equals(ADMConstants.EXTRA_MD5) && !key.equals(consolidationKey)) {
                extrasHashMap.put(key, extras.getString(key));
            }
        }
        final String md5 = ADMSampleMD5ChecksumCalculator.calculateChecksum(extrasHashMap);
        Log.i(TAG, "SampleADMMessageHandlerJobBase:onMessage App md5: " + md5);

        /* Extract md5 from the extras in the intent. */
        final String admMd5 = extras.getString(ADMConstants.EXTRA_MD5);
        Log.i(TAG, "SampleADMMessageHandlerJobBase:onMessage ADM md5: " + admMd5);

        /* Data integrity check. */
        if (!admMd5.trim().equals(md5.trim())) {
            Log.w(TAG, "SampleADMMessageHandlerJobBase:onMessage MD5 checksum verification failure. " +
                    "Message received with errors");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onRegistrationError(final Context context, final String string) {
        Log.e(TAG, "SampleADMMessageHandlerJobBase:onRegistrationError " + string);
        Log.e(TAG, "SampleADMMessageHandlerJobBase:onRegistrationError " + string);
//        Toast.makeText(Const.mainActivity, "onRegistrationError ==33333", Toast.LENGTH_SHORT).show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onRegistered(final Context context, final String registrationId) {
        Log.i(TAG, "SampleADMMessageHandlerJobBase:onRegistered");
        Log.i(TAG, registrationId);

//        Toast.makeText(Const.mainActivity, registrationId + "==44", Toast.LENGTH_SHORT).show();
        /* Register the app instance's registration ID with your server. */
        MyServerMsgHandler srv = new MyServerMsgHandler();
        srv.registerAppInstance(context.getApplicationContext(), registrationId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onUnregistered(final Context context, final String registrationId) {
        Log.i(TAG, "SampleADMMessageHandlerJobBase:onUnregistered");

        /* Unregister the app instance's registration ID with your server. */
        MyServerMsgHandler srv = new MyServerMsgHandler();
        srv.unregisterAppInstance(context.getApplicationContext(), registrationId);
    }

    //The task which you want to execute
    private static class MyTimeTask extends TimerTask {

        public void run() {
            //write your code here
            triggerRebirth(Const.mainActivity);
        }
    }
}
