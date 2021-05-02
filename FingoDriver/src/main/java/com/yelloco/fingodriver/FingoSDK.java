package com.yelloco.fingodriver;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.yelloco.fingodriver.enums.FingoErrorCode;
import com.yelloco.fingodriver.enums.StorageKey;
import com.yelloco.fingodriver.utils.Storage;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

public class FingoSDK
{
    // Constants
    private static final String TAG = FingoSDK.class.getSimpleName();

    // Memebers
    protected static boolean sdkInitialized;
    private static UsbReceiver usbReceiver;
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    private FingoSDK(){
    }

    public static FingoErrorCode initialize(Context context){
        if(sdkInitialized){
            Log.w(TAG, "FingoSDK Already INITIALIZED");
            return FingoErrorCode.H1_DRIVER_INITIALIZED;
        }

        FingoSDK.context = context;

        Storage.getInstance().initialize(context);

        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        if(usbManager == null){
            Log.e(TAG, "Usb Is Not Supported");
            return FingoErrorCode.H1_USB_NOT_SUPPORTED;
        }

        usbReceiver = new UsbReceiver(usbManager, context);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UsbReceiver.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbReceiver.ACTION_USB_DEVICE_DETACHED);
        intentFilter.addAction(UsbReceiver.ACTION_USB_PERMISSION);

        context.registerReceiver(usbReceiver, intentFilter);

        sdkInitialized = true;
        Log.d(TAG, "Fingo SDK Initialized");

        EventBus.getDefault().register(FingoPayDriver.getInstance());

        checkAttachedDevices();

        return FingoErrorCode.H1_OK;
    }

    protected static void checkAttachedDevices() {
        UsbManager usbManager = UsbReceiver.getUsbManager();

        // Get the list of attached devices
        HashMap<String, UsbDevice> devices = usbManager.getDeviceList();

        // Iterate over all devices
        for (String deviceName : devices.keySet()) {
            UsbDevice usbDevice = devices.get(deviceName);
            if(usbDevice != null){
                if(FingoConstants.FINGO_DEVICE_NAME.equals(usbDevice.getProductName())){
                    if(! usbManager.hasPermission(usbDevice)){
                        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context,
                                0, new Intent(UsbReceiver.ACTION_USB_PERMISSION), 0);
                        usbManager.requestPermission(usbDevice, mPermissionIntent);
                    }
                }
            }
        }
    }

    public static void setConsecutiveScanInterval(int delayInMillis){
        if(delayInMillis < FingoConstants.ONE_SECOND){
            delayInMillis = FingoConstants.ONE_SECOND;
        }
        Storage.getInstance().storeInt(StorageKey.CONSECUTIVE_SCAN_INTERVAL.name(), delayInMillis);
    }

    public static String about(){
        return FingoSDK.about(null);
    }

    public static String about(String uniqueID){
        if(FingoConstants.KAN_UNIQUE_ID.equals(uniqueID)){
            return "The Library And It's Rights Belongs to KAN (www.kan4u.com)\nplease contact info@kan4u.com for inquiries";
        }
        else {
            return "Disclaimer and Its rights are returned here";
        }
    }

    public static void destroy() {
        Log.d(TAG, "Is Fingo Initialized: " + sdkInitialized);
        if(! sdkInitialized)
            return;

        sdkInitialized = false;
        EventBus.getDefault().unregister(FingoPayDriver.getInstance());

        if(context != null) {
            context.unregisterReceiver(usbReceiver);
            Log.d(TAG, "Fingo Receiver Unregistered");
        }
    }

    public static boolean isSdkInitialized() {
        return sdkInitialized;
    }
}
