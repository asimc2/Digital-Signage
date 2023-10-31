package com.cleartwo.tvapplication.utils;

import com.cleartwo.tvapplication.ReqResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface APIInterface {

    @GET
    Call<ReqResponse> doGetSchedule(@Header("apikey") String apikey, @Header("apisecret") String apisecret,
                                    @Header("unitid") String unitid, @Url String fileUrl);

    @GET
    Call<ResponseBody> downlload(@Header("apikey") String apikey, @Header("apisecret") String apisecret,
                                 @Header("unitid") String unitid, @Url String fileUrl);

    @GET
    Call<ResponseBody> tokenUpdate(@Header("apikey") String apikey, @Header("apisecret") String apisecret,
                                   @Header("unitid") String unitid, @Header("token") String token,
                                   @Header("type") String type,
                                   @Url String fileUrl);

    @FormUrlEncoded
    @POST("getUnitCode")
    Call<ReqResponse> getUnitCode(@Field("code") String code);

    @FormUrlEncoded
    @POST
    Call<ReqResponse> postReportIP(@Header("apikey") String apikey, @Header("apisecret") String apisecret,
                                   @Header("unitid") String unitid,@Field("ipaddress") String ipaddress,
                                   @Url String fileUrl);

    @FormUrlEncoded
    @POST("postReportStatus")
    Call<ResponseBody> updateAppStatus(@Header("apikey") String apikey, @Header("apisecret") String apisecret,
                                 @Header("unitid") String unitid, @Field("unitId") String unitId,
                                       @Field("name") String name, @Field("version") String version);

    @GET
    Call<ResponseBody> collectionStartUp(@Url String fileUrl);
}