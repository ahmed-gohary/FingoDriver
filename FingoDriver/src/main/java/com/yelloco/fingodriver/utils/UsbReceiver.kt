package com.yelloco.fingodriver.utils

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import com.yelloco.fingodriver.FingoPayDriver
import com.yelloco.fingodriver.models.FingoDevice
import com.yelloco.fingodriver.models.events.DeviceAttachedEvent
import com.yelloco.fingodriver.models.events.DeviceDetachedEvent
import org.greenrobot.eventbus.EventBus

class UsbReceiver(
    usbManager: UsbManager,
    context: Context
): BroadcastReceiver()
{
    init {
        Companion.usbManager = usbManager
        ACTION_USB_PERMISSION = context.packageName + ".USB_PERMISSION"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action: String? = intent?.action

        if(action == null){
            Log.w(TAG, "Action is NULL")
            return
        }

        Log.d(TAG, "Received action: $action")

        when {
            ACTION_USB_PERMISSION == action -> {
                actionUsbPermission(intent)
            }
            ACTION_USB_DEVICE_ATTACHED == action -> {
                actionUsbDeviceAttached(context, intent)
            }
            ACTION_USB_DEVICE_DETACHED == action -> {
                actionUsbDeviceDetached()
            }
        }
    }

    private fun actionUsbPermission(usbPermissionIntent: Intent){
        Log.d(TAG, "actionUsbPermission")
        synchronized(this){
            // reference to the attached device
            val device: UsbDevice? =
                usbPermissionIntent.getParcelableExtra(UsbManager.EXTRA_DEVICE) as UsbDevice?
            val fingoPayDriver: FingoPayDriver = FingoPayDriver.getInstance()
            val fingoDevice: FingoDevice = fingoPayDriver.fingoDevice

            if(usbPermissionIntent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)){
                device?.let {
                    Log.d(TAG, "Device permission granted and is available")
                    fingoDevice.manufacturerName = device.manufacturerName
                    fingoDevice.productName = device.productName
                    fingoDevice.version = device.version
                    fingoDevice.serialNumber = device.serialNumber
                    fingoDevice.isUsagePermissionGranted = true
                } ?: kotlin.run {
                    Log.e(TAG, "Device IS NULL")
                }
            }
            else{
                // reset model to indicate device nullability
                Log.w(TAG, "actionUsbPermission: usb permission rejected")
                fingoDevice.initialize()
            }
            EventBus.getDefault().post(fingoDevice)
        }
    }

    private fun actionUsbDeviceAttached(context: Context?, usbPermissionIntent: Intent){
        Log.d(TAG, "actionUsbDeviceAttached")
        synchronized(this){
            // reference to the attached device
            val usbDevice: UsbDevice? =
                usbPermissionIntent.getParcelableExtra(UsbManager.EXTRA_DEVICE) as UsbDevice?

            if(usbPermissionIntent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)){
                Log.i(TAG, "actionUsbDeviceAttached: Permission Granted")
                if(usbDevice != null){
                    //call method to set up device communication
                }
            }
            else{
                Log.d(TAG, "permission is not granted to device, requesting it....")
                requestUsbPermissionFromExternalDevice(context, usbDevice)
                EventBus.getDefault().post(DeviceAttachedEvent())
            }
        }
    }

    private fun requestUsbPermissionFromExternalDevice(context: Context?, usbDevice: UsbDevice?){
        val permissionIntent: PendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(ACTION_USB_PERMISSION),
            PendingIntent.FLAG_ONE_SHOT
        )

        usbManager.requestPermission(usbDevice, permissionIntent)
    }

    private fun actionUsbDeviceDetached() {
        Log.d(TAG, "actionUsbDeviceDetached")
        synchronized(this) {
            EventBus.getDefault().post(DeviceDetachedEvent())
        }
    }

    companion object {
        // Constants
        private const val TAG = "UsbReceiver"
        const val ACTION_USB_DEVICE_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED"
        const val ACTION_USB_DEVICE_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED"

        // Members
        lateinit var ACTION_USB_PERMISSION: String
        lateinit var usbManager: UsbManager
            private set
    }
}