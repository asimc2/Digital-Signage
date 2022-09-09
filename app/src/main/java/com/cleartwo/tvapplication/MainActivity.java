package com.cleartwo.tvapplication;

import static android.content.ContentValues.TAG;

import static com.cleartwo.tvapplication.Const.DeleteFolder;
import static com.cleartwo.tvapplication.Const.DeleteRecursive;
import static com.cleartwo.tvapplication.Const.fileExist;
import static com.cleartwo.tvapplication.Const.mainActivity;
import static com.cleartwo.tvapplication.utils.APIClient.UNIID;
import static com.cleartwo.tvapplication.utils.APIsCall.getUnitID;
import static com.cleartwo.tvapplication.utils.APIsCall.schedule;
import static com.cleartwo.tvapplication.utils.APIsCall.updatToken;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.amazon.device.messaging.ADM;
import com.amazon.device.messaging.development.ADMManifest;
import com.cleartwo.tvapplication.amazon.MyServerMsgHandler;
import com.cleartwo.tvapplication.amazon.SampleADMMessageHandler;
import com.cleartwo.tvapplication.utils.APIClient;
import com.cleartwo.tvapplication.utils.APIInterface;
import com.cleartwo.tvapplication.utils.SharedPrefHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.sentry.Sentry;

public class MainActivity extends FragmentActivity {

    /**
     * Tag for logs.
     */
    private final static String TAG = "ADMMessenger";

    /**
     * Catches intents sent from the onMessage() callback to update the UI.
     */
    private BroadcastReceiver msgReceiver;

    public Button go;
    public EditText unitId;
    public View firstInstall;
    public VideoView videoView;
    public ImageView imageView;
    public Uri uri;
    public String currentplaylist = "";

    List<ReqResponse.File> reqResponseFile;
    public int order = 1;

    public APIInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Sentry.captureMessage("testing SDK setup");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mainActivity = this;
        apiInterface = APIClient.getClient().create(APIInterface.class);

        // initiate a video view
        videoView = (VideoView) findViewById(R.id.simpleVideoView);

        // initiate a video view
        imageView = (ImageView) findViewById(R.id.imageView);
        initView();

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
//        String path = "android.resource://" + getPackageName() + "/" + R.raw.archies1;
//        uri = Uri.parse(path);
        videoView.setMediaController(null);
//        videoView.setVideoURI(uri);
        videoView.requestFocus();
//        videoView.start();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setVolume(0f, 0f);
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                fileExecution();
            }
        });
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.d("video", "setOnErrorListener ");
                fileExecution();
                return true;
            }
        });
    }

    public void initView() {
        FirebaseApp.initializeApp(this);
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
//        FirebaseMessaging.getInstance().subscribeToTopic("TopicName");
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        //APi Call
                        updatToken(apiInterface, token);

                        // Log and toast
                        @SuppressLint({"StringFormatInvalid", "LocalSuppress"}) String msg = getString(R.string.app_name, token);
                        Log.d(TAG, msg);
                        Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();
                    }
                });

        firstInstall = (View) findViewById(R.id.firstInstall);
        unitId = (EditText) findViewById(R.id.unitId);
//        unitId.setText("e8dcd7ee-1f67-4e9d-8781-05e9dfb666ce");
        go = (Button) findViewById(R.id.go);
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!unitId.getText().toString().trim().equals("")) {
                    getUnitID(apiInterface, unitId.getText().toString().trim(), MainActivity.this);
                } else {
                    Toast.makeText(MainActivity.this, "Unit Id required", Toast.LENGTH_SHORT).show();
                }
            }
        });

        String unitid = (String) SharedPrefHelper.getData(this, "unit_id");
        if (unitid.equals("")) {
            firstInstall.setVisibility(View.VISIBLE);
            unitId.post(new Runnable() {
                @Override
                public void run() {
                    unitId.requestFocus();
                }
            });
        } else {
            UNIID = unitid;
            firstInstall.setVisibility(View.GONE);
            schedule(apiInterface, this);
            timerSchedule();
        }

        /* Register app with ADM. */
        if (Const.isKindle()) {
            try {
                register();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void methodInit(String str) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                videoView.stopPlayback();
                uri = Uri.parse(str);
                File imgFile = new File(str);
                if (imgFile.exists()) {
                    videoView.setVideoURI(uri);
                    videoView.start();
                }
            }
        });
    }

    Handler handler = new Handler();

    public void iniImage(String str) {
        File imgFile = new File(str);
        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imageView.setImageBitmap(myBitmap);
        }

        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() {
            public void run() {
                // yourMethod();
                fileExecution();
            }
        }, 30000);   //30 seconds
    }

    Handler handler1 = new Handler();

    public void timerSchedule() {
        handler1.removeCallbacksAndMessages(null);
        checkSchedule();
        handler1.postDelayed(new Runnable() {
            public void run() {
                // yourMethod();
                timerSchedule();
            }
        }, 10000);   //10 seconds
    }

    public void checkSchedule() {
        ReqResponse data = (ReqResponse) SharedPrefHelper.getSharedOBJECT(this, "my_data");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        Date d = new Date();
        String dayOfTheWeek = sdf.format(d);
        Calendar calendar = Calendar.getInstance();
        int hour24hrs = calendar.get(Calendar.HOUR_OF_DAY);
        int hour12hrs = calendar.get(Calendar.HOUR);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);
        System.out.println("Current hour 24hrs format: " + hour24hrs + ":" + minutes + ":" + seconds);
        System.out.println("Current hour 12hrs format: " + hour12hrs + ":" + minutes + ":" + seconds);

        if (data != null) {
            boolean value = true;
            if (dayOfTheWeek.equals("Monday")) {
                for (int i = 0; i < data.getSchedule().getMonday().size(); i++) {
                    int startHours24 = 0;
                    int endHours24 = 0;
                    if (data.getSchedule().getMonday().get(i).getStart().getHour() == 0) {
                        startHours24 = (24 * 60);
                    } else {
                        startHours24 = (data.getSchedule().getMonday().get(i).getStart().getHour() * 60);
                    }
                    if (data.getSchedule().getMonday().get(i).getEnd().getHour() == 0) {
                        endHours24 = (24 * 60);
                    } else {
                        endHours24 = (data.getSchedule().getMonday().get(i).getEnd().getHour() * 60);
                    }
                    if ((startHours24 + data.getSchedule().getMonday().get(i).getStart().getMin()) <= ((hour24hrs * 60) + minutes) &&
                            (endHours24 + data.getSchedule().getMonday().get(i).getEnd().getMin()) >= ((hour24hrs * 60) + minutes)) {
                        setScheduleFile(data.getSchedule().getMonday().get(i).getPlaylist().getId());
                        value = false;
                        break;
                    }
                }
            } else if (dayOfTheWeek.equals("Tuesday")) {
                for (int i = 0; i < data.getSchedule().getTuesday().size(); i++) {
                    int startHours24 = 0;
                    int endHours24 = 0;
                    if (data.getSchedule().getTuesday().get(i).getStart().getHour() == 0) {
                        startHours24 = (24 * 60);
                    } else {
                        startHours24 = (data.getSchedule().getTuesday().get(i).getStart().getHour() * 60);
                    }
                    if (data.getSchedule().getTuesday().get(i).getEnd().getHour() == 0) {
                        endHours24 = (24 * 60);
                    } else {
                        endHours24 = (data.getSchedule().getTuesday().get(i).getEnd().getHour() * 60);
                    }
                    if ((startHours24 + data.getSchedule().getTuesday().get(i).getStart().getMin()) <= ((hour24hrs * 60) + minutes) &&
                            (endHours24 + data.getSchedule().getTuesday().get(i).getEnd().getMin()) >= ((hour24hrs * 60) + minutes)) {
                        setScheduleFile(data.getSchedule().getTuesday().get(i).getPlaylist().getId());
                        value = false;
                        break;
                    }
                }
            } else if (dayOfTheWeek.equals("Wednesday")) {
                for (int i = 0; i < data.getSchedule().getWednesday().size(); i++) {
                    int startHours24 = 0;
                    int endHours24 = 0;
                    if (data.getSchedule().getWednesday().get(i).getStart().getHour() == 0) {
                        startHours24 = (24 * 60);
                    } else {
                        startHours24 = (data.getSchedule().getWednesday().get(i).getStart().getHour() * 60);
                    }
                    if (data.getSchedule().getWednesday().get(i).getEnd().getHour() == 0) {
                        endHours24 = (24 * 60);
                    } else {
                        endHours24 = (data.getSchedule().getWednesday().get(i).getEnd().getHour() * 60);
                    }
                    if ((startHours24 + data.getSchedule().getWednesday().get(i).getStart().getMin()) <= ((hour24hrs * 60) + minutes) &&
                            (endHours24 + data.getSchedule().getWednesday().get(i).getEnd().getMin()) >= ((hour24hrs * 60) + minutes)) {
                        setScheduleFile(data.getSchedule().getWednesday().get(i).getPlaylist().getId());
                        value = false;
                        break;
                    }
                }
            } else if (dayOfTheWeek.equals("Thursday")) {
                for (int i = 0; i < data.getSchedule().getThursday().size(); i++) {
                    int startHours24 = 0;
                    int endHours24 = 0;
                    if (data.getSchedule().getThursday().get(i).getStart().getHour() == 0) {
                        startHours24 = (24 * 60);
                    } else {
                        startHours24 = (data.getSchedule().getThursday().get(i).getStart().getHour() * 60);
                    }
                    if (data.getSchedule().getThursday().get(i).getEnd().getHour() == 0) {
                        endHours24 = (24 * 60);
                    } else {
                        endHours24 = (data.getSchedule().getThursday().get(i).getEnd().getHour() * 60);
                    }
                    if ((startHours24 + data.getSchedule().getThursday().get(i).getStart().getMin()) <= ((hour24hrs * 60) + minutes) &&
                            (endHours24 + data.getSchedule().getThursday().get(i).getEnd().getMin()) >= ((hour24hrs * 60) + minutes)) {
                        setScheduleFile(data.getSchedule().getThursday().get(i).getPlaylist().getId());
                        value = false;
                        break;
                    }
                }
            } else if (dayOfTheWeek.equals("Friday")) {
                for (int i = 0; i < data.getSchedule().getFriday().size(); i++) {
                    int startHours24 = 0;
                    int endHours24 = 0;
                    if (data.getSchedule().getFriday().get(i).getStart().getHour() == 0) {
                        startHours24 = (24 * 60);
                    } else {
                        startHours24 = (data.getSchedule().getFriday().get(i).getStart().getHour() * 60);
                    }
                    if (data.getSchedule().getFriday().get(i).getEnd().getHour() == 0) {
                        endHours24 = (24 * 60);
                    } else {
                        endHours24 = (data.getSchedule().getFriday().get(i).getEnd().getHour() * 60);
                    }
                    if ((startHours24 + data.getSchedule().getFriday().get(i).getStart().getMin()) <= ((hour24hrs * 60) + minutes) &&
                            (endHours24 + data.getSchedule().getFriday().get(i).getEnd().getMin()) >= ((hour24hrs * 60) + minutes)) {
                        setScheduleFile(data.getSchedule().getFriday().get(i).getPlaylist().getId());
                        value = false;
                        break;
                    }
                }
            } else if (dayOfTheWeek.equals("Saturday")) {
                for (int i = 0; i < data.getSchedule().getSaturday().size(); i++) {
                    int startHours24 = 0;
                    int endHours24 = 0;
                    if (data.getSchedule().getSaturday().get(i).getStart().getHour() == 0) {
                        startHours24 = (24 * 60);
                    } else {
                        startHours24 = (data.getSchedule().getSaturday().get(i).getStart().getHour() * 60);
                    }
                    if (data.getSchedule().getSaturday().get(i).getEnd().getHour() == 0) {
                        endHours24 = (24 * 60);
                    } else {
                        endHours24 = (data.getSchedule().getSaturday().get(i).getEnd().getHour() * 60);
                    }
                    if ((startHours24 + data.getSchedule().getSaturday().get(i).getStart().getMin()) <= ((hour24hrs * 60) + minutes) &&
                            (endHours24 + data.getSchedule().getSaturday().get(i).getEnd().getMin()) >= ((hour24hrs * 60) + minutes)) {
                        setScheduleFile(data.getSchedule().getSaturday().get(i).getPlaylist().getId());
                        value = false;
                        break;
                    }
                }
            } else if (dayOfTheWeek.equals("Sunday")) {
                for (int i = 0; i < data.getSchedule().getSunday().size(); i++) {
                    int startHours24 = 0;
                    int endHours24 = 0;
                    if (data.getSchedule().getSunday().get(i).getStart().getHour() == 0) {
                        startHours24 = (24 * 60);
                    } else {
                        startHours24 = (data.getSchedule().getSunday().get(i).getStart().getHour() * 60);
                    }
                    if (data.getSchedule().getSunday().get(i).getEnd().getHour() == 0) {
                        endHours24 = (24 * 60);
                    } else {
                        endHours24 = (data.getSchedule().getSunday().get(i).getEnd().getHour() * 60);
                    }
                    if ((startHours24 + data.getSchedule().getSunday().get(i).getStart().getMin()) <= ((hour24hrs * 60) + minutes) &&
                            (endHours24 + data.getSchedule().getSunday().get(i).getEnd().getMin()) >= ((hour24hrs * 60) + minutes)) {
                        setScheduleFile(data.getSchedule().getSunday().get(i).getPlaylist().getId());
                        value = false;
                        break;
                    }
                }
            }
            if (value) {
                setScheduleFile(data.getDefaultplaylist().getId());
            }
        }

//        if (data.getSchedule().getFriday())
    }

    public void setScheduleFile(String id) {
        ReqResponse data = (ReqResponse) SharedPrefHelper.getSharedOBJECT(this, "my_data");

        if (data != null) {
            for (int i = 0; i < data.getPlaylists().size(); i++) {
                ReqResponse.Playlist playlist = data.getPlaylists().get(i);
                if (id.equals(playlist.getId()) && !currentplaylist.equals(playlist.getId())) {
                    reqResponseFile = playlist.getFiles();
                    order = 1;
                    fileExecution();
                    currentplaylist = playlist.getId();
                    break;
                }
            }
        }
    }

    public void fileExecution() {
//        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
//                "ProfileImage02");
        File dir = Const.mainActivity.getFilesDir();
        File file = new File(dir, "my_filename");
        boolean ifNotFind = true;
        int maxOrder = 0;
        if (reqResponseFile != null) {
            for (int i = 0; i < reqResponseFile.size(); i++) {
                ReqResponse.File files = reqResponseFile.get(i);

                if (files.getOrder() > maxOrder) {
                    maxOrder = files.getOrder();
                }

                if (files.getOrder() == order) {
                    if (files.getExt().equals("jpg")) {
                        imageView.setVisibility(View.VISIBLE);
                        iniImage(file.getAbsolutePath() + "/" + files.getId());
                    } else {
                        imageView.setVisibility(View.GONE);
                        methodInit(file.getAbsolutePath() + "/" + files.getId());
                    }
                    ifNotFind = false;
                    order += 1;
                    if (order > maxOrder && order > reqResponseFile.size()) {
                        order = 1;
                    }
                    break;
                }

            }
            if (ifNotFind) {
                order += 1;
                fileExecution();
            }
        }
    }

    // Declare the launcher at the top of your Activity/Fragment:
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // FCM SDK (and your app) can post notifications.
                } else {
                    // TODO: Inform user that that your app will not show notifications.
                }
            });


    /**
     * {@inheritDoc}
     */
    @SuppressLint("ResourceType")
    @Override
    public void onResume() {
        /* String to access message field from data JSON. */
        final String msgKey = getString(R.string.json_data_msg_key);

        /* String to access timeStamp field from data JSON. */
        final String timeKey = getString(R.string.json_data_time_key);

        /* Intent action that will be triggered in onMessage() callback. */
        final String intentAction = getString(R.string.intent_msg_action);

        /* Intent category that will be triggered in onMessage() callback. */
        final String msgCategory = getString(R.string.intent_msg_category);

        final Intent nIntent = getIntent();
        if (nIntent != null) {
            /* Extract message from the extras in the intent. */
            final String msg = nIntent.getStringExtra(msgKey);
            final String srvTimeStamp = nIntent.getStringExtra(timeKey);

            /* If msgKey and timeKey extras exist then we're coming from clicking a notification intent. */
            if (msg != null && srvTimeStamp != null) {
                Log.i(TAG, msg);
                /* Display the message in the UI. */
//                final TextView tView = (TextView)findViewById(R.id.textMsgServer);
//                tView.append("Server Time Stamp: " + srvTimeStamp + "\nMessage from server: " + msg + "\n\n");

                /* Clear notifications if any. */
                final NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(getResources().getInteger(12345678));
            }
        }

        /* Listen for messages coming from SampleADMMessageHandler onMessage() callback. */
        msgReceiver = createBroadcastReceiver(msgKey, timeKey);
        final IntentFilter messageIntentFilter = new IntentFilter(intentAction);
        messageIntentFilter.addCategory(msgCategory);
        this.registerReceiver(msgReceiver, messageIntentFilter);
        super.onResume();
    }

    /**
     * Create a {@link BroadcastReceiver} for listening to messages from ADM.
     *
     * @param msgKey  String to access message field from data JSON.
     * @param timeKey String to access timeStamp field from data JSON.
     * @return {@link BroadcastReceiver} for listening to messages from ADM.
     */
    private BroadcastReceiver createBroadcastReceiver(final String msgKey,
                                                      final String timeKey) {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

            /** {@inheritDoc} */
            @SuppressLint("ResourceType")
            @Override
            public void onReceive(final Context context, final Intent broadcastIntent) {
                if (broadcastIntent != null) {

                    /* Extract message from the extras in the intent. */
                    final String msg = broadcastIntent.getStringExtra(msgKey);
                    final String srvTimeStamp = broadcastIntent.getStringExtra(timeKey);

                    if (msg != null && srvTimeStamp != null) {
                        Log.i(TAG, msg);

                        /* Display the message in the UI. */
//                        final TextView tView = (TextView)findViewById(R.id.textMsgServer);
//                        tView.append("Server Time Stamp: " + srvTimeStamp + "\nMessage from server: " + msg + "\n\n");
                    }

                    /* Clear notifications if any. */
                    final NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.cancel(context.getResources().getInteger(12345678));
                }
            }
        };
        return broadcastReceiver;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        this.unregisterReceiver(msgReceiver);
        super.onPause();
    }

    /**
     * Register the app with ADM and send the registration ID to your server
     */
    public void register() {
        final ADM adm = new ADM(this);
        if (adm.isSupported()) {
//            Toast.makeText(MainActivity.this, "==11", Toast.LENGTH_SHORT).show();
            if (adm.getRegistrationId() == null) {
//                Toast.makeText(MainActivity.this, "==22", Toast.LENGTH_SHORT).show();
                adm.startRegister();
            } else {
                /* Send the registration ID for this app instance to your server. */
                /* This is a redundancy since this should already have been performed at registration time from the onRegister() callback */
                /* but we do it because our python server doesn't save registration IDs. */
                final MyServerMsgHandler srv = new MyServerMsgHandler();
                srv.registerAppInstance(getApplicationContext(), adm.getRegistrationId());
                String token = adm.getRegistrationId();
                if (!token.equals("")) {
                    updatToken(apiInterface, token);
                }
                Toast.makeText(MainActivity.this, adm.getRegistrationId(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Unregister the app with ADM.
     * Your server will get notified from the SampleADMMessageHandler:onUnregistered() callback
     */
    private void unregister() {
        final ADM adm = new ADM(this);
        if (adm.isSupported()) {
            if (adm.getRegistrationId() != null) {
                adm.startUnregister();
            }
        }
//        final TextView tView = (TextView)findViewById(R.id.textMsgServer);
//        tView.append("You are now unregistered\n\n");
    }
}