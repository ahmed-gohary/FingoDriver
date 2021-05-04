package com.yelloco.fingodriver.utils;

import android.util.Log;

import com.yelloco.fingodriver.FingoConstants;
import com.yelloco.fingodriver.enums.FingoErrorCode;
import com.yelloco.fingodriver.enums.StorageKey;

public class FingoParams
{
    private static final String TAG = "FingoParams";

    private int consecutiveScanInterval;
    private String cloudUrl;
    private String partnerId;
    private String apiKey;
    private String merchantId;
    private String location;
    private String terminalId;
    private String templateKeySeed;

    public FingoParams(){

    }

    public FingoParams(int consecutiveScanInterval, String cloudUrl, String partnerId, String apiKey, String merchantId, String location, String terminalId, String templateKeySeed) {
        this.consecutiveScanInterval = consecutiveScanInterval;
        this.cloudUrl = cloudUrl;
        this.partnerId = partnerId;
        this.apiKey = apiKey;
        this.merchantId = merchantId;
        this.location = location;
        this.terminalId = terminalId;
        this.templateKeySeed = templateKeySeed;
    }

    public int getConsecutiveScanInterval() {
        return consecutiveScanInterval;
    }

    public void setConsecutiveScanInterval(int delayInMillis) {
        this.consecutiveScanInterval = delayInMillis;
    }

    public String getCloudUrl() {
        return cloudUrl;
    }

    public void setCloudUrl(String cloudUrl) {
        this.cloudUrl = cloudUrl;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getTemplateKeySeed() {
        return templateKeySeed;
    }

    public void setTemplateKeySeed(String templateKeySeed) {
        this.templateKeySeed = templateKeySeed;
    }

    public FingoErrorCode validate(){
        if(this.cloudUrl == null || this.cloudUrl.length() == 0){
            Log.e(TAG, "INVALID CLOUD URL: " + this.cloudUrl);
            return FingoErrorCode.H1_INVALID_CLOUD_URL;
        }

        if(this.partnerId == null || this.partnerId.length() == 0){
            Log.e(TAG, "INVALID PARTNER ID: " + this.partnerId);
            return FingoErrorCode.H1_INVALID_PARTNER_ID;
        }

        if(this.merchantId == null || this.merchantId.length() == 0){
            Log.e(TAG, "INVALID MERCHANT ID: " + this.merchantId);
            return FingoErrorCode.H1_INVALID_MERCHANT_ID;
        }

        if(this.terminalId == null || this.terminalId.length() == 0){
            Log.e(TAG, "INVALID TERMINAL ID: " + this.terminalId);
            return FingoErrorCode.H1_INVALID_TERMINAL_ID;
        }

        if(this.apiKey == null || this.apiKey.length() == 0){
            Log.e(TAG, "INVALID API KEY: " + this.apiKey);
            return FingoErrorCode.H1_INVALID_API_KEY;
        }
        else if(this.apiKey.startsWith("x-apikey")){
            Log.e(TAG, "INVALID API KEY CAN'T START WITH \"x-apikey\": " + this.apiKey);
            return FingoErrorCode.H1_INVALID_API_KEY;
        }

        if(this.templateKeySeed == null || this.templateKeySeed.length() == 0){
            Log.e(TAG, "INVALID TEMPLATE KEY: " + this.templateKeySeed);
            return FingoErrorCode.H1_INVALID_TEMPLATE_KEY;
        }

        if(consecutiveScanInterval < FingoConstants.ONE_SECOND){
            consecutiveScanInterval = FingoConstants.ONE_SECOND;
        }

        Storage.getInstance().storeString(StorageKey.FINGO_CLOUD_URL.name(), this.cloudUrl);
        Storage.getInstance().storeString(StorageKey.PARTNER_ID.name(), this.partnerId);
        Storage.getInstance().storeString(StorageKey.MERCHANT_ID.name(), this.merchantId);
        Storage.getInstance().storeString(StorageKey.TERMINAL_ID.name(), this.terminalId);
        Storage.getInstance().storeString(StorageKey.API_KEY.name(), this.apiKey);
        Storage.getInstance().storeString(StorageKey.TEMPLATE_KEY.name(), this.templateKeySeed);
        Storage.getInstance().storeInt(StorageKey.CONSECUTIVE_SCAN_INTERVAL.name(), consecutiveScanInterval);
        Storage.getInstance().storeString(StorageKey.LOCATION.name(), (location == null) ? "" : location);

        return FingoErrorCode.H1_OK;
    }

    public static FingoParams getStoredParams(){
        FingoParams fingoParams = new FingoParams();
        fingoParams.setCloudUrl(Storage.getInstance().getString(StorageKey.FINGO_CLOUD_URL.name()));
        fingoParams.setPartnerId(Storage.getInstance().getString(StorageKey.PARTNER_ID.name()));
        fingoParams.setMerchantId(Storage.getInstance().getString(StorageKey.MERCHANT_ID.name()));
        fingoParams.setTerminalId(Storage.getInstance().getString(StorageKey.TERMINAL_ID.name()));
        fingoParams.setApiKey(Storage.getInstance().getString(StorageKey.API_KEY.name()));
        fingoParams.setTemplateKeySeed(Storage.getInstance().getString(StorageKey.TEMPLATE_KEY.name()));
        fingoParams.setConsecutiveScanInterval(Storage.getInstance().getInt(StorageKey.CONSECUTIVE_SCAN_INTERVAL.name(), FingoConstants.ONE_SECOND));
        fingoParams.setLocation(Storage.getInstance().getString(StorageKey.LOCATION.name()));

        return fingoParams;
    }
}
