package com.yelloco.fingodriver.models.networking.Enrollment;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.PUT;

public interface EnrollmentApi
{
    @Headers({
            "Authorization: x-apikey 1761900a-bc4b-4406-a0e4-eae4df1a38cd",
            "Content-Type: application/json",
            "x-fingopay-location: Cairo-Building-3000",
            "x-fingopay-terminalid: POS-540-002",
            "x-fingopay-partnerid: kan-dev",
    })
    @PUT("enrolments")
    Call<EnrollmentResponse> enrol(@Body EnrollmentRequest enrollmentRequest);
}
