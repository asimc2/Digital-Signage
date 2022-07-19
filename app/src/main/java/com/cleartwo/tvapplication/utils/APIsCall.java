package com.cleartwo.tvapplication.utils;

import static android.content.ContentValues.TAG;
import static androidx.core.content.PackageManagerCompat.LOG_TAG;
import static com.cleartwo.tvapplication.Const.fileExist;
import static com.cleartwo.tvapplication.Const.writeToDisk;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.cleartwo.tvapplication.Const;
import com.cleartwo.tvapplication.ReqResponse;
import com.google.gson.Gson;

import java.io.File;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class APIsCall {

    //How To Call
    public static void download(APIInterface apiInterface, Boolean B00l, String fileUrl) {

        apiInterface.downlload("gzrZq79Q5A@maxs",
                "vJaZzr%@xTSQP@2MdU3Fn7M*77X!G!",
                "e8dcd7ee-1f67-4e9d-8781-05e9dfb666ce",
                APIClient.BASEURL + "getFile/" + fileUrl).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    Log.d(TAG, "server contacted and has file");

//                        boolean writeToDisk = writeToDisk(response.body());
//                    String path = APIsConst.Companion.getPath(MainActivity.this, "/b84b3649-fb02-4236-95cd-26a0363e3b0b");
//                    APIsConst.Companion.saveFile(response.body(), path);
//                    if (B00l) {
                    assert response.body() != null;
                    writeToDisk(response.body(), fileUrl);
                    Log.d(TAG, "file downloaded ");

                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                            "ProfileImage");
//                    Const.mainActivity.methodInit(file.getAbsolutePath() + "/" + fileUrl);
                    Const.mainActivity.timerSchedule();
//                    }
                } catch (Exception ignored) {
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

    //How To Call
    public static void schedule(APIInterface apiInterface, Context context, Boolean download) {

        apiInterface.doGetSchedule("gzrZq79Q5A@maxs",
                "vJaZzr%@xTSQP@2MdU3Fn7M*77X!G!",
                "e8dcd7ee-1f67-4e9d-8781-05e9dfb666ce",
                APIClient.BASEURL + "getSchedule/e8dcd7ee-1f67-4e9d-8781-05e9dfb666ce").enqueue(new Callback<ReqResponse>() {
            @Override
            public void onResponse(Call<ReqResponse> call, Response<ReqResponse> response) {
                try {
                    Log.d(TAG, response.body().toString());
                    ReqResponse reqResponse = response.body();
                    Gson gson = new Gson();
                    String successResponse = gson.toJson(response.body());
                    Log.d("LOG_TAG", "successResponse: " + successResponse);

                    // get data in your activity
                    ReqResponse data = (ReqResponse) SharedPrefHelper.getSharedOBJECT(context, "my_data");
                    String SAVED = (String) SharedPrefHelper.getData(context, "key_data");

                    // save data in your activity
                    SharedPrefHelper.setSharedOBJECT(context, "my_data", reqResponse);
                    SharedPrefHelper.saveData(context, "key_data", successResponse);

                    String SAVED_NOW = (String) SharedPrefHelper.getData(context, "key_data");

//                    if (reqResponse.getPlaylists() != null && !(SAVED_NOW.equals(SAVED))) {
//                    if (reqResponse.getPlaylists() != null) {
                        for (int i = 0; i < reqResponse.getPlaylists().size(); i++) {

                            for (int j = 0; j < reqResponse.getPlaylists().get(i).getFiles().size(); j++) {
                                APIsCall.download(apiInterface, download, reqResponse.getPlaylists().get(i).getFiles().get(j).getId());
                            }
                        }
//                    }

                    Log.d(TAG, "server contacted and has file");

//                    boolean writeToDisk = writeToDisk(response.body());
//                    String path = APIsConst.Companion.getPath(MainActivity.this, "/b84b3649-fb02-4236-95cd-26a0363e3b0b");
//                    APIsConst.Companion.saveFile(response.body(), path);
//                    assert response.body() != null;
//                    writeToDisk(response.body());
//                    Log.d(TAG, "file downloaded ");
//
//                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
//                            "ProfileImage");
//                    Const.mainActivity.methodInit(file.getAbsolutePath()+"/b84b3649-fb02-4236-95cd-26a0363e3b0b");
                } catch (Exception ignored) {
                }
            }

            @Override
            public void onFailure(Call<ReqResponse> call, Throwable t) {
            }
        });
    }
}
