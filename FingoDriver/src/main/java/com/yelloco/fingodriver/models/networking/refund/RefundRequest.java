package com.yelloco.fingodriver.models.networking.refund;

import com.yelloco.fingodriver.models.networking.payment.PosData;

public class RefundRequest
{
    private String merchantId;
    private String verificationTemplate;
    private String transactionIdToRefund;
    private String gatewayTransactionIdToRefund;
    private int refundAmount;
    private TerminalData terminalData;

    public RefundRequest(){
    }

    public RefundRequest(String merchantId, String verificationTemplate, String transactionIdToRefund, String gatewayTransactionIdToRefund, int refundAmount, TerminalData terminalData) {
        this.merchantId = merchantId;
        this.verificationTemplate = verificationTemplate;
        this.transactionIdToRefund = transactionIdToRefund;
        this.gatewayTransactionIdToRefund = gatewayTransactionIdToRefund;
        this.refundAmount = refundAmount;
        this.terminalData = terminalData;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getVerificationTemplate() {
        return verificationTemplate;
    }

    public void setVerificationTemplate(String verificationTemplate) {
        this.verificationTemplate = verificationTemplate;
    }

    public String getTransactionIdToRefund() {
        return transactionIdToRefund;
    }

    public void setTransactionIdToRefund(String transactionIdToRefund) {
        this.transactionIdToRefund = transactionIdToRefund;
    }

    public String getGatewayTransactionIdToRefund() {
        return gatewayTransactionIdToRefund;
    }

    public void setGatewayTransactionIdToRefund(String gatewayTransactionIdToRefund) {
        this.gatewayTransactionIdToRefund = gatewayTransactionIdToRefund;
    }

    public int getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(int refundAmount) {
        this.refundAmount = refundAmount;
    }

    public TerminalData getTerminalData() {
        return terminalData;
    }

    public void setTerminalData(TerminalData terminalData) {
        this.terminalData = terminalData;
    }

    @Override
    public String toString() {
        return "RefundRequest{" +
                "merchantId='" + merchantId + '\'' +
                ", verificationTemplate='" + verificationTemplate + '\'' +
                ", transactionIdToRefund='" + transactionIdToRefund + '\'' +
                ", gatewayTransactionIdToRefund='" + gatewayTransactionIdToRefund + '\'' +
                ", refundAmount=" + refundAmount +
                ", terminalData=" + terminalData +
                '}';
    }
}
