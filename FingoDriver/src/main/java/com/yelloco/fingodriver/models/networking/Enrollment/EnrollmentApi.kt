package com.yelloco.fingodriver.models.networking.Enrollment

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.PUT

interface EnrollmentApi
{
    @PUT("enrolments")
    fun enrol(
        @HeaderMap headers: Map<String, String?>?,
        @Body enrollmentRequest: EnrollmentRequest?
    ): Call<EnrollmentResponse?>?
}