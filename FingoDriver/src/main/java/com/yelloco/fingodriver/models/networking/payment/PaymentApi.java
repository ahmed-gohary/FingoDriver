package com.yelloco.fingodriver.models.networking.payment;


import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface PaymentApi
{
    @POST("payments")
    Call<PaymentResponse> pay(@HeaderMap Map<String, String> headers, @Body PaymentRequest paymentRequest);
}
