package com.yelloco.fingodriver;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.yelloco.fingodriver.callbacks.FingoRequestLogger;
import com.yelloco.fingodriver.enums.FingoErrorCode;
import com.yelloco.fingodriver.enums.StorageKey;
import com.yelloco.fingodriver.utils.FingoParams;
import com.yelloco.fingodriver.utils.FingoUsbManager;
import com.yelloco.fingodriver.utils.Storage;
import com.yelloco.fingodriver.utils.UsbReceiver;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

public class FingoSDK
{
    // Constants
    private static final String TAG = FingoSDK.class.getSimpleName();

    // Memebers
    protected static boolean sdkInitialized;
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private static FingoUsbManager fingoUsbManager;
    private static FingoRequestLogger fingoRequestLogger;

    private FingoSDK(){
    }

    public static FingoErrorCode initialize(Context context, FingoParams fingoParams){
        if(sdkInitialized){
            Log.w(TAG, "FingoSDK Already INITIALIZED");
            return FingoErrorCode.H1_DRIVER_INITIALIZED;
        }
        if(context == null){
            Log.e(TAG, "FingoSDK received NULL context");
            return FingoErrorCode.H1_UNEXPECTED;
        }

        FingoSDK.context = context;
        Storage.getInstance().initialize(context);

        FingoErrorCode fingoParamsErrorCode = fingoParams.validate();

        if(fingoParamsErrorCode != FingoErrorCode.H1_OK){
            return fingoParamsErrorCode;
        }

        fingoUsbManager = new FingoUsbManager(context);
        FingoErrorCode usbInitStatus = fingoUsbManager.initialize();

        if(usbInitStatus != FingoErrorCode.H1_OK){
            return usbInitStatus;
        }

        sdkInitialized = true;
        Log.d(TAG, "Fingo SDK Initialized");

        EventBus.getDefault().register(FingoPayDriver.getInstance());

        fingoUsbManager.checkAttachedDevices();

        return FingoErrorCode.H1_OK;
    }

    public static FingoErrorCode initialize(Context context, FingoParams fingoParams, FingoRequestLogger fingoRequestLogger){
        FingoSDK.initialize(context, fingoParams);
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

        fingoUsbManager.destroy();
    }

    public static boolean isSdkInitialized() {
        return sdkInitialized;
    }
}
