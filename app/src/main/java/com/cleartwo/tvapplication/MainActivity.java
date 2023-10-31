package com.cleartwo.tvapplication;

import static com.cleartwo.tvapplication.Const.mainActivity;
import static com.cleartwo.tvapplication.utils.APIClient.UNIID;
import static com.cleartwo.tvapplication.utils.APIsCall.getUnitID;
import static com.cleartwo.tvapplication.utils.APIsCall.schedule;
import static com.cleartwo.tvapplication.utils.APIsCall.updatToken;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.amazon.device.messaging.ADM;
import com.cleartwo.tvapplication.amazon.MyServerMsgHandler;
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
import java.util.Objects;

import io.sentry.Sentry;
import top.ss007.library.DownloadListener;
import top.ss007.library.DownloadUtil;
import top.ss007.library.InputParameter;

public class MainActivity extends AppCompatActivity {

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
    public TextView clears;
    public View firstInstall;
    public VideoView videoView;
    public ImageView imageView;
    public WebView webUrl;
    public Uri uri;
    public String currentplaylist = "";

    List<ReqResponse.File> reqResponseFile;
    public int order = 1;

    public APIInterface apiInterface;
    ProgressDialog progressdialog;

//    private AppUpdateManager mAppUpdateManager;
//    private static final int RC_APP_UPDATE = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();
        Sentry.captureMessage("testing SDK setup");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mainActivity = this;
        apiInterface = APIClient.getClient().create(APIInterface.class);

        // initiate a video view
        videoView = (VideoView) findViewById(R.id.simpleVideoView);
        imageView = (ImageView) findViewById(R.id.imageView);
        webUrl = (WebView) findViewById(R.id.webUrl);
        initView();
//        startSystemAlertWindowPermission();

        progressdialog = new ProgressDialog(this);
        progressdialog.setMessage("Downloading files. Please wait...");
        progressdialog.getWindow().setGravity(Gravity.TOP);
        WindowManager.LayoutParams params = progressdialog.getWindow().getAttributes();
        params.y = 100;
        progressdialog.getWindow().setAttributes(params);

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(null);
        videoView.requestFocus();
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

    @SuppressLint("SetTextI18n")
    public void initView() {
        storageMethod();
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
        clears = (TextView) findViewById(R.id.clears);
        clears.setText("Powered by ClearTwo: " + BuildConfig.VERSION_NAME);
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

    }

    private void storageMethod() {
        double totalSize = new File(getApplicationContext().getFilesDir().getAbsoluteFile().toString()).getTotalSpace();
        double totMb = totalSize / (1024 * 1024);
        double availableSize = new File(getApplicationContext().getFilesDir().getAbsoluteFile().toString()).getFreeSpace();
        double freeMb = availableSize / (1024 * 1024);
        long freeBytesExternal = new File(getExternalFilesDir(null).toString()).getFreeSpace();
        int free = (int) (freeBytesExternal / (1024 * 1024));
        long totaSize = new File(getExternalFilesDir(null).toString()).getTotalSpace();
        int total = (int) (totaSize / (1024 * 1024));
        String availableMb = free + "Mb out of " + total + "MB";
        Log.e("totMb = ", String.valueOf(totMb));
        Log.e("freeMb = ", String.valueOf(freeMb));
        Log.e("availableMb = ", availableMb);
    }

    public void methodInit(String str) {
        webUrl.setVisibility(View.GONE);
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
        webUrl.setVisibility(View.GONE);
        File imgFile = new File(str);
        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imageView.setImageBitmap(myBitmap);
        }

        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() {
            public void run() {
                fileExecution();
            }
        }, 30000);   //30 seconds
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void initUrl(String str, String tm) {
        webUrl.setVisibility(View.VISIBLE);
        WebSettings webSettings = webUrl.getSettings();
        webUrl.setWebViewClient(new WebViewClient());
        webSettings.setJavaScriptEnabled(true);
        webUrl.loadUrl(str);

        int time = Integer.parseInt(tm);
        time = time * 1000;
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() {
            public void run() {
                fileExecution();
            }
        }, time);   //30 seconds
    }

    Handler handler1 = new Handler();

    public void timerSchedule() {
        handler1.removeCallbacksAndMessages(null);
        checkSchedule();
        handler1.postDelayed(new Runnable() {
            public void run() {
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
        try {
            progressdialog.dismiss();
        } catch (Exception ignored) {
        }

        File file = new File(getExternalFilesDir(null).getAbsolutePath());
        boolean ifNotFind = true;
        int maxOrder = 0;
        if (reqResponseFile != null) {
            for (int i = 0; i < reqResponseFile.size(); i++) {
                ReqResponse.File files = reqResponseFile.get(i);

                if (files.getOrder() > maxOrder) {
                    maxOrder = files.getOrder();
                }

                if (files.getOrder() == order) {
                    ifNotFind = false;
                    order += 1;
                    if (order > maxOrder && order > reqResponseFile.size()) {
                        order = 1;
                    }
                    if (files.getExt().equals("jpg") || files.getExt().equals("png")) {
                        setText(imageView, true);
                        iniImage(file.getAbsolutePath() + "/" + files.getId());
                    } else if (files.getExt().equals("mkv")) {
                        setText(imageView, false);
                        methodInit(file.getAbsolutePath() + "/" + files.getId());
                    } else if (files.getExt().equals("url")) {
                        setText(imageView, false);
                        initUrl(files.getUrl(), files.getTime());
                    } else {
                        fileExecution();
                    }
                    break;
                }

            }
            if (ifNotFind) {
                order += 1;
                try {
//                    fileExecution();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
        this.finish();
        super.onPause();
    }

    /**
     * Unregister the app with ADM.
     * Your server will get notified from the SampleADMMessageHandler:onUnregistered() callback
     */
    private void setText(final ImageView imageView, final Boolean value) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (value) {
                    imageView.setVisibility(View.VISIBLE);
                } else {
                    imageView.setVisibility(View.GONE);
                }
            }
        });
    }

    private final static String BASE_URL = "https://video-dev.cleartwo.uk/api/v1/getFile/";
    public static String FILE_URL = "";
    public String desFilePath;

    public void startDownload(String desFilePath) {
//        download.setEnabled(false);
        DownloadUtil.getInstance()
                .downloadFile(new InputParameter.Builder(BASE_URL, FILE_URL, desFilePath)
                        .setCallbackOnUiThread(true)
                        .build(), new DownloadListener() {
                    @Override
                    public void onFinish(final File file) {
                        Const.mainActivity.currentplaylist = "";
                        Const.mainActivity.order = 1;
                        Const.mainActivity.timerSchedule();
                        Log.e("Tag", "下载的文件地址为:\n" + file.getAbsolutePath());
                        schedule(apiInterface, MainActivity.this);
                        try {
                            progressdialog.dismiss();
                        } catch (Exception ignored) {
                        }
                    }

                    @Override
                    public void onProgress(int progress, long downloadedLengthKb, long totalLengthKb) {
                        try {
                            progressdialog.show();
                        } catch (Exception ignored) {
                        }
                        Log.e("Tag",
                                String.format("文件文件下载进度：%d%s \n\n已下载:%sKB | 总长:%sKB",
                                        progress, "%",
                                        downloadedLengthKb + "",
                                        totalLengthKb + ""));
                    }

                    @Override
                    public void onFailed(String errMsg) {
                    }
                });
    }

}