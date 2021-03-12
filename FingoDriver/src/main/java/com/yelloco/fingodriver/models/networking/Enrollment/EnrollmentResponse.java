package com.yelloco.fingodriver.models.networking.Enrollment;

import com.google.gson.annotations.SerializedName;

public class EnrollmentResponse
{
    @SerializedName("kan-dev:" + "fp_member_id")
    private String memberId;

    @SerializedName("kan-dev:" + "fp_veinid_id")
    private String veinId;

    public EnrollmentResponse() {
    }

    public EnrollmentResponse(String memberId, String veinId) {
        this.memberId = memberId;
        this.veinId = veinId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getVeinId() {
        return veinId;
    }

    public void setVeinId(String veinId) {
        this.veinId = veinId;
    }
}
