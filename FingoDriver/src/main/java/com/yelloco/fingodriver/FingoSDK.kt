package com.yelloco.fingodriver

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.yelloco.fingodriver.callbacks.FingoRequestLogger
import com.yelloco.fingodriver.enums.FingoErrorCode
import com.yelloco.fingodriver.enums.StorageKey
import com.yelloco.fingodriver.utils.FingoParams
import com.yelloco.fingodriver.utils.FingoUsbManager
import com.yelloco.fingodriver.utils.Storage
import com.yelloco.fingodriver.utils.Storage.storeBoolean
import org.greenrobot.eventbus.EventBus

object FingoSDK
{
    // Constants
    private val TAG = FingoSDK::class.java.simpleName

    // Memebers
    var isSdkInitialized = false
        internal set

    @SuppressLint("StaticFieldLeak")
    private var fingoUsbManager: FingoUsbManager? = null

    var fingoRequestLogger: FingoRequestLogger? = null

    fun initialize(context: Context?): FingoErrorCode {
        if (isSdkInitialized) {
            Log.w(TAG, "FingoSDK Already INITIALIZED")
            return FingoErrorCode.H1_DRIVER_INITIALIZED
        }
        if (context == null) {
            Log.e(TAG, "FingoSDK received NULL context")
            return FingoErrorCode.H1_UNEXPECTED
        }

        Storage.initialize(context)
        storeBoolean(StorageKey.PARAMS_STATUS.name, false)
        fingoUsbManager = FingoUsbManager(context)
        val usbInitStatus = fingoUsbManager!!.initialize()
        if (usbInitStatus !== FingoErrorCode.H1_OK) {
            return usbInitStatus
        }
        isSdkInitialized = true
        Log.d(TAG, "Fingo SDK Initialized")
        EventBus.getDefault().register(FingoPayDriver)
        fingoUsbManager!!.checkAttachedDevices()
        return FingoErrorCode.H1_OK
    }

    fun initialize(context: Context?, fingoRequestLogger: FingoRequestLogger?): FingoErrorCode {
        FingoSDK.fingoRequestLogger = fingoRequestLogger
        return initialize(context)
    }

    fun setFingoParams(fingoParams: FingoParams): FingoErrorCode {
        val fingoParamsErrorCode = fingoParams.validate()
        if (fingoParamsErrorCode === FingoErrorCode.H1_OK) {
            fingoParams.loadParams()
        } else {
            storeBoolean(StorageKey.PARAMS_STATUS.name, false)
        }
        Log.i(TAG, "setFingoParams validation result: $fingoParamsErrorCode")
        return fingoParamsErrorCode
    }

    fun about(): String {
        return about(null)
    }

    fun about(uniqueID: String?): String {
        return if (FingoFactory.Constants.KAN_UNIQUE_ID == uniqueID) {
            "The Library And It's Rights Belongs to KAN (www.kan4u.com)\nplease contact info@kan4u.com for inquiries"
        } else {
            "Disclaimer and Its rights are returned here"
        }
    }

    fun destroy() {
        Log.d(TAG, "Is Fingo Initialized: $isSdkInitialized")
        if (!isSdkInitialized) return
        isSdkInitialized = false
        EventBus.getDefault().unregister(FingoPayDriver)
        fingoUsbManager!!.destroy()
    }
}