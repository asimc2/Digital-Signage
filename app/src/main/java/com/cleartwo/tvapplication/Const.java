package com.cleartwo.tvapplication;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;

import okhttp3.ResponseBody;

public class Const {
    @SuppressLint("StaticFieldLeak")
    public static MainActivity mainActivity;

    public static boolean isKindle(){
        final String AMAZON = "Amazon";

        return (Build.MANUFACTURER.equals(AMAZON));
    }

    public static boolean writeToDisk(ResponseBody body, String path) {
        try {
//            File mediaStorageDir = new File(
//                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
//                    "ProfileImage02");

            File dir = Const.mainActivity.getFilesDir();
            File file = new File(dir, "my_filename");

            // Create the storage directory if it does not exist
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    Log.e("ProfileImage02", "Oops! Failed create "
                            + "ProfileImage02" + " directory");
                }
            }
            File futureStudioIconFile = new File(file.getPath() + File.separator
                    + path);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                }

                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean fileExist(Context context, String fname) {
//        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
//                "ProfileImage02" + "/" + fname);
        File dir = Const.mainActivity.getFilesDir();
        File file = new File(dir, "my_filename" + "/" + fname);
        return file.exists();

    }

    public static void DeleteRecursive(String strPath) {

        File fileOrDirectory = new File(strPath);

        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles())
                DeleteRecursive(child.getPath());
            fileOrDirectory.delete();
        } else {

            fileOrDirectory.delete();
        }

        if (fileOrDirectory.isDirectory())
        {
            String[] children = fileOrDirectory.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(fileOrDirectory, children[i]).delete();
            }
        }
    }

    public static boolean DeleteFolder(Context context) {

        String path1 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "";
        DeleteRecursive(path1);

//        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
//        File file = new File(dir, "ProfileImage02");
        File dir = Const.mainActivity.getFilesDir();
        File file = new File(dir, "my_filename" );
        return file.delete();
    }

    /**
     * CHECK WHETHER INTERNET CONNECTION IS AVAILABLE OR NOT
     */
    public static boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) Const.mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

}
