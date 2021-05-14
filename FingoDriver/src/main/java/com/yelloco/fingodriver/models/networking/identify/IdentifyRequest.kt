package com.yelloco.fingodriver.models.networking.identify

class IdentifyRequest
{
    var verificationTemplate: String? = null

    constructor() {}
    constructor(verificationTemplate: String?) {
        this.verificationTemplate = verificationTemplate
    }

    override fun toString(): String {
        return "IdentifyRequest{" +
                "verificationTemplate='" + verificationTemplate!!.substring(0, 10) + '\'' +
                '}'
    }
}