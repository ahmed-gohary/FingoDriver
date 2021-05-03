package com.yelloco.fingodriver.models.networking.Enrollment;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.PUT;

public interface EnrollmentApi
{
    @PUT("enrolments")
    Call<EnrollmentResponse> enrol(@HeaderMap Map<String, String> headers, @Body EnrollmentRequest enrollmentRequest);
}
