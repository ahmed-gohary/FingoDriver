package com.yelloco.fingodriver.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.util.Log
import com.yelloco.fingodriver.enums.FingoErrorCode

class FingoUsbManager(private val context: Context)
{
    fun initialize(): FingoErrorCode
    {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager?

        if (usbManager == null) {
            Log.e(TAG, "Usb Is Not Supported")
            return FingoErrorCode.H1_USB_NOT_SUPPORTED
        }

        usbReceiver = UsbReceiver(usbManager, context)

        val intentFilter = IntentFilter()
        intentFilter.addAction(UsbReceiver.ACTION_USB_DEVICE_ATTACHED)
        intentFilter.addAction(UsbReceiver.ACTION_USB_DEVICE_DETACHED)
        intentFilter.addAction(UsbReceiver.ACTION_USB_PERMISSION)
        context.registerReceiver(usbReceiver, intentFilter)

        return FingoErrorCode.H1_OK
    }

    fun checkAttachedDevices() {
        val usbManager = UsbReceiver.usbManager

        // Get the list of attached devices
        val devices = usbManager.deviceList

        // Iterate over all devices
        for (deviceName in devices.keys) {
            val usbDevice = devices[deviceName]
            if (usbDevice != null) {
                if (FingoConstants.FINGO_DEVICE_NAME == usbDevice.productName) {
                    if (!usbManager.hasPermission(usbDevice)) {
                        // if the device doesn't have permission then ask for it
                        Log.d(TAG, "Requesting permission for device for the first time")
                    } else {
                        // permission already granted start using the device
                        Log.d(TAG, "Permission already granted for device, connecting...")
                    }
                    val mPermissionIntent = PendingIntent.getBroadcast(
                        context,
                        0, Intent(UsbReceiver.ACTION_USB_PERMISSION), 0
                    )
                    usbManager.requestPermission(usbDevice, mPermissionIntent)
                }
            }
        }
    }

    fun destroy() {
        usbReceiver?.let {
            context.unregisterReceiver(usbReceiver)
            Log.d(TAG, "Fingo Receiver Unregistered")
        } ?: kotlin.run {
            Log.w(TAG, "USBReceiver not registered")
        }
    }

    companion object {
        private const val TAG = "FingoUsbManager"
        private var usbReceiver: UsbReceiver? = null
    }
}