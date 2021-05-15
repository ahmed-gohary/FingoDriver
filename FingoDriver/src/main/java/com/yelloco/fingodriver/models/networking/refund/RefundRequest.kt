package com.yelloco.fingodriver.models.networking.refund

class RefundRequest
{
    var merchantId: String? = null
    var verificationTemplate: String? = null
    var transactionIdToRefund: String? = null
    var gatewayTransactionIdToRefund: String? = null
    var refundAmount = 0
    var terminalData: TerminalData? = null

    constructor() {}
    constructor(
        merchantId: String?,
        verificationTemplate: String?,
        transactionIdToRefund: String?,
        gatewayTransactionIdToRefund: String?,
        refundAmount: Int,
        terminalData: TerminalData?
    ) {
        this.merchantId = merchantId
        this.verificationTemplate = verificationTemplate
        this.transactionIdToRefund = transactionIdToRefund
        this.gatewayTransactionIdToRefund = gatewayTransactionIdToRefund
        this.refundAmount = refundAmount
        this.terminalData = terminalData
    }

    override fun toString(): String {
        return "RefundRequest{" +
                "merchantId='" + merchantId + '\'' +
                ", verificationTemplate='" + verificationTemplate!!.substring(0, 10) + '\'' +
                ", transactionIdToRefund='" + transactionIdToRefund + '\'' +
                ", gatewayTransactionIdToRefund='" + gatewayTransactionIdToRefund + '\'' +
                ", refundAmount=" + refundAmount +
                ", terminalData=" + terminalData +
                '}'
    }
}