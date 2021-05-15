package com.yelloco.fingodriver.models.networking.refund

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface RefundApi
{
    @POST("refunds")
    fun refund(
        @HeaderMap headers: Map<String, String?>,
        @Body refundRequest: RefundRequest
    ): Call<RefundResponse?>?
}