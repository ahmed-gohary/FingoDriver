package com.yelloco.fingodriver.models.fingo_operation;

public class PaymentData
{
    private String transactionId;
    private String gatewayTransactionId;
    private String gatewayAuthCode;
    private String maskedCardNumber;
    private String timestamp;
    private boolean paymentStatus;

    public PaymentData(){
    }

    public PaymentData(String transactionId, String gatewayTransactionId, String gatewayAuthCode, String maskedCardNumber, String timestamp) {
        this.transactionId = transactionId;
        this.gatewayTransactionId = gatewayTransactionId;
        this.gatewayAuthCode = gatewayAuthCode;
        this.maskedCardNumber = maskedCardNumber;
        this.timestamp = timestamp;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getGatewayTransactionId() {
        return gatewayTransactionId;
    }

    public void setGatewayTransactionId(String gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
    }

    public String getGatewayAuthCode() {
        return gatewayAuthCode;
    }

    public void setGatewayAuthCode(String gatewayAuthCode) {
        this.gatewayAuthCode = gatewayAuthCode;
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    public void setMaskedCardNumber(String maskedCardNumber) {
        this.maskedCardNumber = maskedCardNumber;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(boolean paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    @Override
    public String toString() {
        return "PaymentData{" +
                "transactionId='" + transactionId + '\'' +
                ", gatewayTransactionId='" + gatewayTransactionId + '\'' +
                ", gatewayAuthCode='" + gatewayAuthCode + '\'' +
                ", maskedCardNumber='" + maskedCardNumber + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", paymentStatus=" + paymentStatus +
                '}';
    }
}
