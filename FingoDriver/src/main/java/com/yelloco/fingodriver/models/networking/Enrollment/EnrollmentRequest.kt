package com.yelloco.fingodriver.models.networking.Enrollment

class EnrollmentRequest
{
    var hand = 0
    var finger: String? = null
    var enrolmentTemplate: String? = null
    var verificationTemplate: String? = null

    constructor() {}
    constructor(
        hand: Int,
        finger: String?,
        enrolmentTemplate: String?,
        verificationTemplate: String?
    ) {
        this.hand = hand
        this.finger = finger
        this.enrolmentTemplate = enrolmentTemplate
        this.verificationTemplate = verificationTemplate
    }

    override fun toString(): String {
        return "EnrollmentRequest{" +
                "hand=" + hand +
                ", finger='" + finger + '\'' +
                ", enrolmentTemplate='" + enrolmentTemplate!!.substring(0, 10) + '\'' +
                ", verificationTemplate='" + verificationTemplate!!.substring(0, 10) + '\'' +
                '}'
    }
}