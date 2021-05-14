package com.yelloco.fingodriver.models.networking.payment

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface PaymentApi
{
    @POST("payments")
    fun pay(
        @HeaderMap headers: Map<String?, String?>?,
        @Body paymentRequest: PaymentRequest?
    ): Call<PaymentResponse?>?
}