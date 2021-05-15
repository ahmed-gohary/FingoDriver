package com.yelloco.fingodriver.models.networking.Enrollment

import com.google.gson.annotations.SerializedName

class EnrollmentResponse
{
    @SerializedName("kan-dev:" + "fp_member_id")
    var memberId: String? = null

    @SerializedName("kan-dev:" + "fp_veinid_id")
    var veinId: String? = null

    constructor() {}
    constructor(memberId: String?, veinId: String?) {
        this.memberId = memberId
        this.veinId = veinId
    }

    override fun toString(): String {
        return "EnrollmentResponse{" +
                "memberId='" + memberId + '\'' +
                ", veinId='" + veinId + '\'' +
                '}'
    }
}