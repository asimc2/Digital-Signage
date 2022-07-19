package com.cleartwo.tvapplication.utils;

import com.cleartwo.tvapplication.ReqResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface APIInterface {

    @GET
    Call<ReqResponse> doGetSchedule(@Header("apikey") String apikey, @Header("apisecret") String apisecret, @Header("unitid") String unitid, @Url String fileUrl);

    @GET
    Call<ResponseBody> downlload(@Header("apikey") String apikey, @Header("apisecret") String apisecret, @Header("unitid") String unitid, @Url String fileUrl);

//    @POST("/api/users")
//    Call<User> createUser(@Body User user);
//
//    @GET("/api/users?")
//    Call<UserList> doGetUserList(@Query("page") String page);
//
//    @FormUrlEncoded
//    @POST("/api/users?")
//    Call<UserList> doCreateUserWithField(@Field("name") String name, @Field("job") String job);
}

