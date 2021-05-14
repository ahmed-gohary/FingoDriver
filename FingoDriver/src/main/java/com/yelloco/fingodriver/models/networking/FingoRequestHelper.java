package com.yelloco.fingodriver.models.networking;

import android.util.Log;

import com.yelloco.fingodriver.enums.StorageKey;
import com.yelloco.fingodriver.utils.Storage;

import java.util.HashMap;
import java.util.Map;

public class FingoRequestHelper
{
    private Map<String, String> headers;
    private String merchantId;
    private String fingoCloudBaseUrl;

    public FingoRequestHelper(){
        this.headers = new HashMap<>();
        this.fillHeaderData();
    }

    private void fillHeaderData(){
        this.fillApiKey();
        this.fillLocation();
        this.fillTerminalId();
        this.fillPartnerId();
        this.fillMerchantId();
        this.fillCloudUrl();
        this.fillDriverVersion();
        this.fillBuildNumberVersion();

        Log.d("FingoRequestHelper", this.toString());
    }

    private void fillApiKey() {
        // add api_key
        String api_key = Storage.getString(StorageKey.API_KEY.name());
        if(api_key != null && !api_key.startsWith("x-apikey")){
            api_key = "x-apikey " + api_key;
        }
        this.headers.put(Key.AUTHORIZATION.getValue(), api_key);
    }

    private void fillLocation() {
        // add location
        String location = Storage.getString(StorageKey.LOCATION.name());
        this.headers.put(Key.LOCATION.getValue(), location);
    }

    private void fillTerminalId() {
        // add TerminalID
        String terminalId = Storage.getString(StorageKey.TERMINAL_ID.name());
        this.headers.put(Key.TERMINAL_ID.getValue(), terminalId);
    }

    private void fillPartnerId() {
        // add PartnerID
        String partnerId = Storage.getString(StorageKey.PARTNER_ID.name());
        this.headers.put(Key.PARTNER_ID.getValue(), partnerId);
    }

    private void fillMerchantId(){
        this.merchantId = Storage.getString(StorageKey.MERCHANT_ID.name());
    }

    private void fillCloudUrl(){
        this.fingoCloudBaseUrl = Storage.getString(StorageKey.FINGO_CLOUD_URL.name());
    }

    private void fillDriverVersion(){
        this.headers.put(Key.CLOUD_DRIVER_VERSION.getValue(), "2.0");
    }

    private void fillBuildNumberVersion(){
        this.headers.put(Key.BUILD_NUMBER_ASSEMBLY_VERSION.getValue(), "1234ABCD6789ABEF");
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public String getMerchantId() {
        return this.merchantId;
    }

    public String getFingoCloudBaseUrl() {
        return this.fingoCloudBaseUrl;
    }

    private enum Key {
        AUTHORIZATION("Authorization"),
        LOCATION("x-fingopay-location"),
        TERMINAL_ID("x-fingopay-terminalid"),
        PARTNER_ID("x-fingopay-partnerid"),
        CLOUD_DRIVER_VERSION("x-fingo-driver-version"),
        BUILD_NUMBER_ASSEMBLY_VERSION("x-fingo-driver-assembly"),
        ;

        private final String value;

        Key(String value){
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @Override
    public String toString() {
        return "FingoRequestHelper{" +
                "headers=" + headers +
                ", merchantId='" + merchantId + '\'' +
                ", fingoCloudBaseUrl='" + fingoCloudBaseUrl + '\'' +
                '}';
    }
}
