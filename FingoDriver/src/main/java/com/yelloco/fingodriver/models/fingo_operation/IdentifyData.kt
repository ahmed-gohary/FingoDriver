package com.yelloco.fingodriver.models.fingo_operation

class IdentifyData
{
    var verificationTemplate: String? = null
    var enrolmentTemplate: String? = null
    var veinId: String? = null
    var memberId: String? = null
    var isOnlineData = false

    override fun toString(): String {
        return "ResponseData{" +
                "verificationTemplate='" + verificationTemplate + '\'' +
                ", enrolmentTemplate='" + enrolmentTemplate + '\'' +
                ", veinId='" + veinId + '\'' +
                ", memberId='" + memberId + '\'' +
                ", isOnlineData=" + isOnlineData +
                '}'
    }
}