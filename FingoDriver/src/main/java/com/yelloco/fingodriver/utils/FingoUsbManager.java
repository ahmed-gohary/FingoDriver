package com.yelloco.fingodriver.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.yelloco.fingodriver.FingoConstants;
import com.yelloco.fingodriver.enums.FingoErrorCode;

import java.util.HashMap;

public class FingoUsbManager
{
    private static final String TAG = "FingoUsbManager";

    private Context context;
    private static UsbReceiver usbReceiver;

    public FingoUsbManager(Context context){
        this.context = context;
    }

    public FingoErrorCode initialize(){
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

        return FingoErrorCode.H1_OK;
    }

    public void checkAttachedDevices() {
        UsbManager usbManager = UsbReceiver.getUsbManager();

        // Get the list of attached devices
        HashMap<String, UsbDevice> devices = usbManager.getDeviceList();

        // Iterate over all devices
        for (String deviceName : devices.keySet()) {
            UsbDevice usbDevice = devices.get(deviceName);
            if(usbDevice != null){
                if(FingoConstants.FINGO_DEVICE_NAME.equals(usbDevice.getProductName())){
                    if(! usbManager.hasPermission(usbDevice)){
                        // if the device doesn't have permission then ask for it
                        Log.d(TAG, "Requesting permission for device for the first time");
                    }
                    else {
                        // permission already granted start using the device
                        Log.d(TAG, "Permission already granted for device, connecting...");
                    }

                    PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context,
                            0, new Intent(UsbReceiver.ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(usbDevice, mPermissionIntent);
                }
            }
        }
    }

    public void destroy() {
        if(context != null) {
            context.unregisterReceiver(usbReceiver);
            Log.d(TAG, "Fingo Receiver Unregistered");
        }
    }
}
