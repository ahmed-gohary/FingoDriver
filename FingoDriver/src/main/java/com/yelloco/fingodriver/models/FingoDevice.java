package com.yelloco.fingodriver.models;

import com.hitachi.fv.android.h1client.H1Client;
import com.yelloco.fingodriver.FingoConstants;

public class FingoDevice
{
    private String manufacturerName;
    private String productName;
    private String version;
    private String serialNumber;
    private H1Client h1Client;
    private boolean usagePermissionGranted;
    private boolean deviceOpened;
    private boolean fingoDevice;

    public FingoDevice(){
        initialize();
    }

    public FingoDevice(String manufacturerName, String productName, String version, String serialNumber, H1Client h1Client, boolean usagePermissionGranted, boolean deviceOpened) {
        this.manufacturerName = manufacturerName;
        this.productName = productName;
        this.version = version;
        this.serialNumber = serialNumber;
        this.h1Client = h1Client;
        this.usagePermissionGranted = usagePermissionGranted;
        this.deviceOpened = deviceOpened;
        this.fingoDevice = FingoConstants.FINGO_DEVICE_NAME.equals(this.productName);
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
        this.fingoDevice = FingoConstants.FINGO_DEVICE_NAME.equals(this.productName);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public boolean isUsagePermissionGranted() {
        return usagePermissionGranted;
    }

    public void setUsagePermissionGranted(boolean usagePermissionGranted) {
        this.usagePermissionGranted = usagePermissionGranted;
    }

    public H1Client getH1Client() {
        return h1Client;
    }

    public void setH1Client(H1Client h1Client) {
        this.h1Client = h1Client;
    }

    public boolean isDeviceOpened() {
        return deviceOpened;
    }

    public void setDeviceOpened(boolean deviceOpened) {
        this.deviceOpened = deviceOpened;
    }

    public boolean isFingoDevice() {
        return fingoDevice;
    }

    public void initialize() {
        this.manufacturerName = "YelloCo";
        this.productName = "Yello 7";
        this.version = "UNKNOWN";
        this.serialNumber = "UNKNOWN";
        this.h1Client = new H1Client();
        this.usagePermissionGranted = false;
        this.deviceOpened = false;
        this.fingoDevice = false;
    }
}
