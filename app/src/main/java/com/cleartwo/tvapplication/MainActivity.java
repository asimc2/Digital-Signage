package com.cleartwo.tvapplication;

import static android.content.ContentValues.TAG;

import static com.cleartwo.tvapplication.Const.fileExist;
import static com.cleartwo.tvapplication.Const.mainActivity;
import static com.cleartwo.tvapplication.utils.APIsCall.schedule;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.cleartwo.tvapplication.utils.APIClient;
import com.cleartwo.tvapplication.utils.APIInterface;
import com.cleartwo.tvapplication.utils.SharedPrefHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MainActivity extends FragmentActivity {

    public VideoView videoView;
    public ImageView imageView;
    public Uri uri;
    public String currentplaylist = "";

    List<ReqResponse.File> reqResponseFile;
    int order = 1;

    APIInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;

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

                        // Log and toast
                        @SuppressLint({"StringFormatInvalid", "LocalSuppress"}) String msg = getString(R.string.app_name, token);
                        Log.d(TAG, msg);
                        Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();
                    }
                });

        // initiate a video view
        imageView = (ImageView) findViewById(R.id.imageView);

        // initiate a video view
//        VideoView simpleVideoView = (VideoView) findViewById(R.id.simpleVideoView);
//        simpleVideoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video));
        videoView = (VideoView) findViewById(R.id.simpleVideoView);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        String path = "android.resource://" + getPackageName() + "/" + R.raw.archies1;
        uri = Uri.parse(path);
        videoView.setMediaController(null);
        videoView.setVideoURI(uri);
        videoView.requestFocus();
        videoView.start();
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                // Do whatever u need to do here
//                File path = Environment.getExternalStorageDirectory();
//                File file = new File(path, "file_name.jpg");
//                videoView.start();
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

        apiInterface = APIClient.getClient().create(APIInterface.class);
        if (!fileExist(this, "b84b3649-fb02-4236-95cd-26a0363e3b0b")) {
            schedule(apiInterface, this, true);
        } else {
            schedule(apiInterface, this, false);
//            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
//                    "ProfileImage");
//            methodInit(file.getAbsolutePath() + "/b84b3649-fb02-4236-95cd-26a0363e3b0b");
            // Image initialization
//            iniImage(file.getAbsolutePath() + "/3462b287-130b-4396-9411-764d475624e1");
            // Delete ProfileImage folder on first install
//            String path1 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)+"/ProfileImage";
//            DeleteRecursive(path1);
            timerSchedule();
        }
    }

    public void methodInit(String str) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                videoView.stopPlayback();
                uri = Uri.parse(str);
                videoView.setVideoURI(uri);
                videoView.start();
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
        }, 10000);   //30 seconds
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
                    }else {
                        startHours24 = (data.getSchedule().getMonday().get(i).getStart().getHour() * 60);
                    }
                    if (data.getSchedule().getMonday().get(i).getEnd().getHour() == 0) {
                        endHours24 = (24 * 60);
                    }else {
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
                    }else {
                        startHours24 = (data.getSchedule().getTuesday().get(i).getStart().getHour() * 60);
                    }
                    if (data.getSchedule().getTuesday().get(i).getEnd().getHour() == 0) {
                        endHours24 = (24 * 60);
                    }else {
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
                    }else {
                        startHours24 = (data.getSchedule().getWednesday().get(i).getStart().getHour() * 60);
                    }
                    if (data.getSchedule().getWednesday().get(i).getEnd().getHour() == 0) {
                        endHours24 = (24 * 60);
                    }else {
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
                    }else {
                        startHours24 = (data.getSchedule().getThursday().get(i).getStart().getHour() * 60);
                    }
                    if (data.getSchedule().getThursday().get(i).getEnd().getHour() == 0) {
                        endHours24 = (24 * 60);
                    }else {
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
                    }else {
                        startHours24 = (data.getSchedule().getFriday().get(i).getStart().getHour() * 60);
                    }
                    if (data.getSchedule().getFriday().get(i).getEnd().getHour() == 0) {
                        endHours24 = (24 * 60);
                    }else {
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
                    }else {
                        startHours24 = (data.getSchedule().getSaturday().get(i).getStart().getHour() * 60);
                    }
                    if (data.getSchedule().getSaturday().get(i).getEnd().getHour() == 0) {
                        endHours24 = (24 * 60);
                    }else {
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
                    }else {
                        startHours24 = (data.getSchedule().getSunday().get(i).getStart().getHour() * 60);
                    }
                    if (data.getSchedule().getSunday().get(i).getEnd().getHour() == 0) {
                        endHours24 = (24 * 60);
                    }else {
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

    public void fileExecution() {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "ProfileImage");
        for (int i = 0; i < reqResponseFile.size(); i++) {
            ReqResponse.File files = reqResponseFile.get(i);

            if (files.getOrder() == order) {
                if (files.getExt().equals("jpg")) {
                    imageView.setVisibility(View.VISIBLE);
                    iniImage(file.getAbsolutePath() + "/" + files.getId());
                } else {
                    imageView.setVisibility(View.GONE);
                    methodInit(file.getAbsolutePath() + "/" + files.getId());
                }
                order += 1;
                if (order > reqResponseFile.size())
                    order = 1;
                break;
            } else {
//                order = 1;
//                videoView.start();
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


}