package com.yelloco.fingodriver.models.networking.refund;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface RefundApi
{
    @POST("refunds")
    Call<RefundResponse> refund(@HeaderMap Map<String, String> headers, @Body RefundRequest refundRequest);
}
