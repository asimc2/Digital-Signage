package com.cleartwo.tvapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.cleartwo.tvapplication.ReqResponse;
import com.google.gson.Gson;

// SharedPrefHelper is a class contains the get and save sharedPrefernce data
public class SharedPrefHelper {

    // save data in sharedPrefences
    public static void setSharedOBJECT(Context context, String key,
                                       Object value) {

        SharedPreferences sharedPreferences =  context.getSharedPreferences(
                context.getPackageName(), Context.MODE_PRIVATE);

        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(value);
        prefsEditor.putString(key, json);
        prefsEditor.apply();
    }

    // get data from sharedPrefences
    public static Object getSharedOBJECT(Context context, String key) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getPackageName(), Context.MODE_PRIVATE);

        Gson gson = new Gson();
        String json = sharedPreferences.getString(key, "");
        Object obj = gson.fromJson(json, Object.class);
        if (obj == null){
            return null;
        }else {
            try {
                ReqResponse objData = new Gson().fromJson(obj.toString(), ReqResponse.class);
                return objData;
            }catch (Exception e) {
                ReqResponse objData = new Gson().fromJson(json, ReqResponse.class);
                return objData;
            }
        }
    }
    public static void saveData(Context context, String key,String value) {
        SharedPreferences sharedPreferences =  context.getSharedPreferences(
                context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor .putString(key, value);
        prefsEditor.commit();
    }

    public static String getData(Context context, String key) {
        SharedPreferences sharedPreferences =  context.getSharedPreferences(
                context.getPackageName(), Context.MODE_PRIVATE);
        if (sharedPreferences!= null) {
            return sharedPreferences.getString(key, "");
        }
        return "";
    }

}

