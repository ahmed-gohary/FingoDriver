package com.yelloco.fingodriver.models.networking.payment;

public class PaymentRequest
{
    private String merchantId;
    private String verificationTemplate;
    private int totalAmount;
    private int totalDiscount;
    private String currency;
    private PosData posData;

    public PaymentRequest(){
    }

    public PaymentRequest(String merchantId, String verificationTemplate, int totalAmount, int totalDiscount, String currency, PosData posData) {
        this.merchantId = merchantId;
        this.verificationTemplate = verificationTemplate;
        this.totalAmount = totalAmount;
        this.totalDiscount = totalDiscount;
        this.currency = currency;
        this.posData = posData;
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

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(int totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PosData getPosData() {
        return posData;
    }

    public void setPosData(PosData posData) {
        this.posData = posData;
    }

    @Override
    public String toString() {
        return "PaymentRequest{" +
                "merchantId='" + merchantId + '\'' +
                ", verificationTemplate='" + verificationTemplate.substring(0, 10) + '\'' +
                ", totalAmount=" + totalAmount +
                ", totalDiscount=" + totalDiscount +
                ", currency='" + currency + '\'' +
                ", posData=" + posData.toString() +
                '}';
    }
}
