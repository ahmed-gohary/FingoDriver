package com.yelloco.fingodriver.models.fingo_operation

class PaymentData
{
    var transactionId: String? = null
    var gatewayTransactionId: String? = null
    var gatewayAuthCode: String? = null
    var maskedCardNumber: String? = null
    var timestamp: String? = null
    var paymentStatus = false

    constructor() {}
    constructor(
        transactionId: String?,
        gatewayTransactionId: String?,
        gatewayAuthCode: String?,
        maskedCardNumber: String?,
        timestamp: String?
    ) {
        this.transactionId = transactionId
        this.gatewayTransactionId = gatewayTransactionId
        this.gatewayAuthCode = gatewayAuthCode
        this.maskedCardNumber = maskedCardNumber
        this.timestamp = timestamp
    }

    override fun toString(): String {
        return "PaymentData{" +
                "transactionId='" + transactionId + '\'' +
                ", gatewayTransactionId='" + gatewayTransactionId + '\'' +
                ", gatewayAuthCode='" + gatewayAuthCode + '\'' +
                ", maskedCardNumber='" + maskedCardNumber + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", paymentStatus=" + paymentStatus +
                '}'
    }
}