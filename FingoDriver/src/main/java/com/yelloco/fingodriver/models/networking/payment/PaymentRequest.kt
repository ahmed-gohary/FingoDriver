package com.yelloco.fingodriver.models.networking.payment

class PaymentRequest
{
    var merchantId: String? = null
    var verificationTemplate: String? = null
    private var totalAmount = 0
    var totalDiscount = 0
    var currency: String? = null
    var posData: PosData? = null

    constructor() {}
    constructor(
        merchantId: String?,
        verificationTemplate: String?,
        totalAmount: Int,
        totalDiscount: Int,
        currency: String?,
        posData: PosData?
    ) {
        this.merchantId = merchantId
        this.verificationTemplate = verificationTemplate
        this.totalAmount = totalAmount
        this.totalDiscount = totalDiscount
        this.currency = currency
        this.posData = posData
    }

    fun getTotalAmount(): Double {
        return totalAmount.toDouble()
    }

    fun setTotalAmount(totalAmount: Int) {
        this.totalAmount = totalAmount
    }

    override fun toString(): String {
        return "PaymentRequest{" +
                "merchantId='" + merchantId + '\'' +
                ", verificationTemplate='" + verificationTemplate!!.substring(0, 10) + '\'' +
                ", totalAmount=" + totalAmount +
                ", totalDiscount=" + totalDiscount +
                ", currency='" + currency + '\'' +
                ", posData=" + posData.toString() +
                '}'
    }
}