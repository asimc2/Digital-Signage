package com.cleartwo.tvapplication.utils;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {

    private static Retrofit retrofit = null;
    public static String BASEURL = "https://video-dev.cleartwo.uk/api/v1/";
    public static String UNIID = "e8dcd7ee-1f67-4e9d-8781-05e9dfb666ce";
    public static String APISECRET = "vJaZzr%@xTSQP@2MdU3Fn7M*77X!G!";
    public static String APIKEY = "gzrZq79Q5A@maxs";

    public static Retrofit getClient() {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASEURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit;
    }
}
