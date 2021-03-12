package com.yelloco.fingodriver.models.networking.refund;

public class RefundResponse
{
    private String transactionId;
    private String gatewayTransactionId;
    private String gatewayAuthCode;
    private String maskedCardNumber;
    private String merchantId;
    private String timestamp;

    public RefundResponse(){
    }

    public RefundResponse(String transactionId, String gatewayTransactionId, String gatewayAuthCode, String maskedCardNumber, String merchantId, String timestamp) {
        this.transactionId = transactionId;
        this.gatewayTransactionId = gatewayTransactionId;
        this.gatewayAuthCode = gatewayAuthCode;
        this.maskedCardNumber = maskedCardNumber;
        this.merchantId = merchantId;
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

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "RefundResponse{" +
                "transactionId='" + transactionId + '\'' +
                ", gatewayTransactionId='" + gatewayTransactionId + '\'' +
                ", gatewayAuthCode='" + gatewayAuthCode + '\'' +
                ", maskedCardNumber='" + maskedCardNumber + '\'' +
                ", merchantId='" + merchantId + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
