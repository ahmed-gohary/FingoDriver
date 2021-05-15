package com.yelloco.fingodriver.models.networking.refund

class RefundResponse
{
    var transactionId: String? = null
    var gatewayTransactionId: String? = null
    var gatewayAuthCode: String? = null
    var maskedCardNumber: String? = null
    var merchantId: String? = null
    var timestamp: String? = null

    constructor() {}
    constructor(
        transactionId: String?,
        gatewayTransactionId: String?,
        gatewayAuthCode: String?,
        maskedCardNumber: String?,
        merchantId: String?,
        timestamp: String?
    ) {
        this.transactionId = transactionId
        this.gatewayTransactionId = gatewayTransactionId
        this.gatewayAuthCode = gatewayAuthCode
        this.maskedCardNumber = maskedCardNumber
        this.merchantId = merchantId
        this.timestamp = timestamp
    }

    override fun toString(): String {
        return "RefundResponse{" +
                "transactionId='" + transactionId + '\'' +
                ", gatewayTransactionId='" + gatewayTransactionId + '\'' +
                ", gatewayAuthCode='" + gatewayAuthCode + '\'' +
                ", maskedCardNumber='" + maskedCardNumber + '\'' +
                ", merchantId='" + merchantId + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}'
    }
}