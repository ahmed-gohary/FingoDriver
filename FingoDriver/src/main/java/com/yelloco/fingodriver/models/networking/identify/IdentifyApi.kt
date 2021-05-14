package com.yelloco.fingodriver.models.networking.identify

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface IdentifyApi
{
    @POST("identify")
    fun identify(
        @HeaderMap headers: Map<String?, String?>?,
        @Body identifyRequest: IdentifyRequest?
    ): Call<IdentifyResponse?>?
}