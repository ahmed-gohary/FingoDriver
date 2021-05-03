package com.yelloco.fingodriver.models.networking.identify;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IdentifyApi
{
    @POST("identify")
    Call<IdentifyResponse> identify(@HeaderMap Map<String, String> headers, @Body IdentifyRequest identifyRequest);
}
