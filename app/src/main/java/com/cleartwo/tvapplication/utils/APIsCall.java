package com.cleartwo.tvapplication.utils;

import static android.content.ContentValues.TAG;
import static com.cleartwo.tvapplication.Const.writeToDisk;
import static com.cleartwo.tvapplication.utils.APIClient.APIKEY;
import static com.cleartwo.tvapplication.utils.APIClient.APISECRET;
import static com.cleartwo.tvapplication.utils.APIClient.UNIID;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;


import com.cleartwo.tvapplication.BuildConfig;
import com.cleartwo.tvapplication.Const;
import com.cleartwo.tvapplication.MainActivity;
import com.cleartwo.tvapplication.ReqResponse;
import com.google.android.gms.common.util.ArrayUtils;
import com.google.gson.Gson;

import java.io.File;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class APIsCall {
    public static boolean clear_cache = false;

    //How To Call
    public static void schedule(APIInterface apiInterface, Context context) {

        apiInterface.doGetSchedule(APIKEY,
                APISECRET,
                UNIID,
                APIClient.BASEURL + "getSchedule/" + UNIID).enqueue(new Callback<ReqResponse>() {
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

                    for (int i = 0; i < reqResponse.getPlaylists().size(); i++) {

                        for (int j = 0; j < reqResponse.getPlaylists().get(i).getFiles().size(); j++) {

//                            Const.mainActivity.currentplaylist = "";
//                            File dir = Const.mainActivity.getFilesDir();
//                            File file = new File(dir, "my_filename");
//                            File imgFile = new File(file.getAbsolutePath() + "/" + reqResponse.getPlaylists().get(i).getFiles().get(j).getId());
//                            if (!imgFile.exists()) {
//                                APIsCall.download(apiInterface, reqResponse.getPlaylists().get(i).getFiles().get(j).getId());
//                            }

                            Const.mainActivity.desFilePath = Const.mainActivity.getExternalFilesDir(null).getAbsolutePath() + "/" + reqResponse.getPlaylists().get(i).getFiles().get(j).getId();
                            File imgFile = new File(Const.mainActivity.desFilePath);
                            MainActivity.FILE_URL = reqResponse.getPlaylists().get(i).getFiles().get(j).getId();
                            if (!imgFile.exists()) {
                                Const.mainActivity.startDownload(Const.mainActivity.desFilePath);
                                break;
                            } else if (clear_cache) {
                                File myObj = new File(Const.mainActivity.desFilePath);
                                if (myObj.delete()) {
                                    System.out.println("Deleted the file: " + myObj.getName());
                                } else {
                                    System.out.println("Failed to delete the file.");
                                }
                            }
                        }
                    }

                    if (clear_cache) {
                        clear_cache = false;
                        schedule(apiInterface, context);
                    }
                    Log.d(TAG, "server contacted and has file");
                } catch (Exception e) {
                    Toast.makeText(context, "Some thing went wrong", Toast.LENGTH_SHORT).show();

//                    SharedPrefHelper.saveData(context, "unit_id", "");
//                    Const.mainActivity.initView();

                }
            }

            @Override
            public void onFailure(Call<ReqResponse> call, Throwable t) {
                Toast.makeText(context, "Some thing went wrong", Toast.LENGTH_SHORT).show();
//                SharedPrefHelper.saveData(context, "unit_id", "");
//                Const.mainActivity.initView();
            }
        });
    }

    //How To Call
    public static boolean onTimeCall = true;

    public static void updatToken(APIInterface apiInterface, String token) {
        String device = "Android";
        if (Const.isKindle()) {
            device = "Amazon";
        }

        apiInterface.tokenUpdate(APIKEY,
                APISECRET,
                UNIID,
                token,
                device,
                APIClient.BASEURL + "reportToken/" + UNIID
        ).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    Log.d(TAG, "server contacted and has file");
                    assert response.body() != null;
                    if (onTimeCall) {
                        sendReportIP(apiInterface);
                        onTimeCall = false;
                    } else {
                        onTimeCall = true;
                    }
                    Log.d(TAG, "file downloaded ");
                    CollectionStartUp(apiInterface);
                } catch (Exception ignored) {
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

    //How To Call
    public static void getUnitID(APIInterface apiInterface, String code, Context context) {
        ProgressDialog progressdialog = new ProgressDialog(context);
        progressdialog.setMessage("Please Wait....");
        progressdialog.show();
        apiInterface.getUnitCode(code).enqueue(new Callback<ReqResponse>() {
            @Override
            public void onResponse(Call<ReqResponse> call, Response<ReqResponse> response) {
                try {
                    Log.d(TAG, "server contacted and has file");
                    ReqResponse resBody = response.body();
                    assert resBody != null;
                    if (resBody.getMessage().equals("success")) {
                        SharedPrefHelper.saveData(context, "unit_id", resBody.getData());
//                        UNIID = resBody.getData();
//                        Const.mainActivity.firstInstall.setVisibility(View.GONE);
//                        schedule(apiInterface, context);
                        Const.mainActivity.initView();
                    } else {
                        Toast.makeText(context, resBody.getData(), Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception ignored) {
                    Toast.makeText(context, "Some thing went wrong, Try again later.", Toast.LENGTH_SHORT).show();
                }
                progressdialog.dismiss();
            }

            @Override
            public void onFailure(Call<ReqResponse> call, Throwable t) {
                progressdialog.dismiss();
                Toast.makeText(context, "Some thing went wrong, Try again later.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //How To Call
    public static void sendReportIP(APIInterface apiInterface) {
        String myIP = getLocalIpAddress();

        apiInterface.postReportIP(APIKEY,
                APISECRET,
                UNIID,
                myIP,
                APIClient.BASEURL + "reportIP/" + UNIID).enqueue(new Callback<ReqResponse>() {
            @Override
            public void onResponse(Call<ReqResponse> call, Response<ReqResponse> response) {
                try {
                    Log.d(TAG, "server contacted and has file");

                    ReqResponse resBody = response.body();
                    Log.d(TAG, "" + resBody);
                    Log.d(TAG, "" + resBody);
                } catch (Exception ignored) {
                }
            }

            @Override
            public void onFailure(Call<ReqResponse> call, Throwable t) {
            }
        });
    }
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    //How To Call
    public static void updateStatus(APIInterface apiInterface) {
        Log.d(TAG, "server contacted and request is report_status");
        String versionName = BuildConfig.VERSION_NAME;
        String deviceName = android.os.Build.MODEL;
        apiInterface.updateAppStatus(APIKEY,
                APISECRET,
                UNIID,
                UNIID,
                deviceName,
                versionName).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    Log.d(TAG, "server contacted and has file");

                    ResponseBody resBody = response.body();
                    Log.d(TAG, "" + resBody);
                    Log.d(TAG, "" + resBody);
                } catch (Exception ignored) {
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

    //How To Call
    public static void CollectionStartUp(APIInterface apiInterface) {
        Log.d(TAG, "server contacted and request is report_status");
        apiInterface.collectionStartUp(
                APIClient.BASEURL + "startUp/" + UNIID).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    Log.d(TAG, "server contacted and has file");

                    ResponseBody resBody = response.body();
                    Log.d(TAG, "" + resBody);
                    Log.d(TAG, "" + resBody);
                } catch (Exception ignored) {
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }
}
