package com.yelloco.fingodriver;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.yelloco.fingodriver.exceptions.FingoSDKException;
import com.yelloco.fingodriver.models.FingoDevice;
import com.yelloco.fingodriver.models.events.DeviceAttachedEvent;
import com.yelloco.fingodriver.models.events.DeviceDetachedEvent;
import com.yelloco.fingodriver.enums.FingoErrorCode;

import org.greenrobot.eventbus.EventBus;

class UsbReceiver extends BroadcastReceiver
{
    // Constants
    private static final String TAG = UsbReceiver.class.getSimpleName();
    public static final String ACTION_USB_DEVICE_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public static final String ACTION_USB_DEVICE_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    public static String ACTION_USB_PERMISSION = ".USB_PERMISSION";

    // Members
    private static UsbManager usbManager;

    public UsbReceiver(UsbManager usbManager, Context context){
        UsbReceiver.usbManager = usbManager;
        ACTION_USB_PERMISSION = context.getPackageName() + "." + ACTION_USB_PERMISSION;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if(action == null){
            Log.w(TAG, "Action is NULL");
            return;
        }

        Log.d(TAG, "Received action: " + action);

        if(ACTION_USB_PERMISSION.equals(action)){
            actionUsbPermission(context, intent);
        }
        else if(ACTION_USB_DEVICE_ATTACHED.equals(action)){
            actionUsbDeviceAttached(context, intent);
        }
        else if(ACTION_USB_DEVICE_DETACHED.equals(action)){
            actionUsbDeviceDetached(context, intent);
        }
    }

    private void actionUsbPermission(Context context, Intent usbPermissionIntent){
        Log.d(TAG, "actionUsbPermission");
        synchronized (this){
            UsbDevice device = (UsbDevice) usbPermissionIntent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            FingoPayDriver fingoPayDriver = FingoPayDriver.getInstance();
            FingoDevice fingoDevice = fingoPayDriver.getFingoDevice();
            if(usbPermissionIntent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                if(device != null){
                    Log.d(TAG, "Device permission granted and is available");
                    fingoDevice.setManufacturerName(device.getManufacturerName());
                    fingoDevice.setProductName(device.getProductName());
                    fingoDevice.setVersion(device.getVersion());
                    fingoDevice.setSerialNumber(device.getSerialNumber());
                    fingoDevice.setUsagePermissionGranted(true);
                }
                else{
                    // reset model to indicate device nullability
                    Log.w(TAG, "Device permission denied, resetting device");
                    fingoDevice.initialize();
                }
            }
            else{
                Log.w(TAG, "actionUsbPermission: usb permission rejected");
                fingoDevice.initialize();
            }
            EventBus.getDefault().post(fingoDevice);
        }
    }


    private void actionUsbDeviceAttached(Context context, Intent usbPermissionIntent){
        Log.d(TAG, "actionUsbDeviceAttached");
        synchronized (this) {
            // reference to the attached device
            UsbDevice usbDevice = (UsbDevice) usbPermissionIntent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

            if (usbPermissionIntent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                Log.i(TAG, "actionUsbDeviceAttached: Permission Granted");
                if(usbDevice != null){
                    //call method to set up device communication
                }
            }
            else {
                Log.d(TAG, "permission is not granted to device, requesting it....");
                requestUsbPermissionFromExternalDevice(context, usbDevice, usbPermissionIntent);
                EventBus.getDefault().post(new DeviceAttachedEvent());
            }
        }
    }

    private void requestUsbPermissionFromExternalDevice(Context context, UsbDevice usbDevice, Intent usbPermissionIntent){
        // request permission to access external device
        PendingIntent permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_ONE_SHOT);
        usbManager.requestPermission(usbDevice, permissionIntent);
    }

    private void actionUsbDeviceDetached(Context context, Intent usbPermissionIntent){
        Log.d(TAG, "actionUsbDeviceDetached");
        synchronized (this){
            EventBus.getDefault().post(new DeviceDetachedEvent());
        }
    }

    public static UsbManager getUsbManager(){
        return UsbReceiver.usbManager;
    }
}
