package com.yelloco.fingodriver

import android.util.Log
import android.util.Pair
import com.hitachi.fv.android.h1client.CryptoAlg
import com.hitachi.fv.android.h1client.H1Client
import com.hitachi.fv.android.h1client.H1ClientException
import com.hitachi.fv.android.h1client.SecurityLevel
import com.yelloco.fingodriver.callbacks.FingoCaptureCallback
import com.yelloco.fingodriver.enums.FingoErrorCode
import com.yelloco.fingodriver.enums.StorageKey
import com.yelloco.fingodriver.models.FingoDevice
import com.yelloco.fingodriver.models.events.DeviceDetachedEvent
import com.yelloco.fingodriver.utils.Storage
import com.yelloco.fingodriver.utils.UsbReceiver
import org.greenrobot.eventbus.Subscribe

object FingoPayDriver
{
    // Constants
    private const val TAG = "FingoPayDriver"
    private const val DEFAULT_TIMEOUT = FingoFactory.Constants.TWENTY_SECS
    val CONSECUTIVE_SCAN_RESTING_INTERVAL: Long = Storage.getLong(
        StorageKey.CONSECUTIVE_SCAN_INTERVAL.name, FingoFactory.Constants.ONE_SECOND)

    // Members
    private val fingoDevice: FingoDevice = FingoDevice()
    private var scannerResting: Boolean = false
    private var captureSessionActive: Boolean = false
    private var templateKeySeedSet: Boolean = false
    private var isLockActive: Boolean = false

    // getters
    val activeDevice: FingoDevice
        get() = fingoDevice

    private fun getH1Client(): H1Client? {
        return if(this.fingoDevice.isUsagePermissionGranted){
            this.fingoDevice.h1Client
        }
        else{
            return null
        }
    }
    
    fun setCryptoTemplateKey(templateKey: String): FingoErrorCode {
        return this.setCryptoTemplateKey(templateKey, CryptoAlg.AES256)
    }

    fun setCryptoTemplateKey(templateKey: String, cryptoAlg: CryptoAlg): FingoErrorCode {
        val h1Client = getH1Client() ?: return FingoErrorCode.H1_DEVICE_NOT_FOUND

        try {
            h1Client.setTemplateKeySeed(templateKey.toByteArray(), cryptoAlg);
            this.templateKeySeedSet = true;
            Log.d(TAG, "Template key set successfully")
            return FingoErrorCode.H1_OK;
        }
        catch (h1Exception: H1ClientException){
            this.templateKeySeedSet = false
            Log.e(TAG, "Failed to set template key $h1Exception")
            h1Exception.printStackTrace();
            return FingoErrorCode.parseErrorCode(h1Exception.h1Error);
        }
        catch (e: Exception){
            this.templateKeySeedSet = false
            Log.e(TAG, "Failed to set template key $e");
            e.printStackTrace();
            return FingoErrorCode.H1_INVALID_TEMPLATE_KEY;
        }
    }

    fun openDevice(): FingoErrorCode {
        val templateKeySeed = Storage.getString(StorageKey.TEMPLATE_KEY.name)
            ?: return FingoErrorCode.H1_INVALID_TEMPLATE_KEY

        val templateKeySeedErrorCode = this.setCryptoTemplateKey(templateKeySeed)
        if(templateKeySeedErrorCode != FingoErrorCode.H1_OK){
            return templateKeySeedErrorCode
        }

        val h1Client = getH1Client()
            ?: return FingoErrorCode.H1_DEVICE_NOT_FOUND

        return try {
            val usbManager = UsbReceiver.usbManager
            val usbDevice = h1Client.getDevice(usbManager)
            h1Client.openDevice(usbManager, usbDevice)
            fingoDevice.isDeviceOpened = true
            Log.d(TAG, "Fingo device opened successfully")
            FingoErrorCode.H1_OK
        }
        catch (e: H1ClientException) {
            Log.e(TAG, "Failed to open Fingo device: $e")
            fingoDevice.isDeviceOpened = false
            e.printStackTrace()
            FingoErrorCode.parseErrorCode(e.h1Error)
        }
        catch (illegalStateException: IllegalStateException) {
            Log.e(TAG, "Failed to open Fingo device, already opened: $illegalStateException")
            fingoDevice.isDeviceOpened = false
            illegalStateException.printStackTrace()
            FingoErrorCode.H1_DEVICE_ALREADY_OPENED
        }
    }

    fun capture(fingoCaptureCallback: FingoCaptureCallback?): Pair<FingoErrorCode, ByteArray?> {
        return this.capture(DEFAULT_TIMEOUT, fingoCaptureCallback)
    }

    fun capture(timeout: Long): Pair<FingoErrorCode, ByteArray?> {
        return this.capture(timeout, null)
    }

    fun capture(
        timeoutInMillis: Long = DEFAULT_TIMEOUT,
        fingoCaptureCallback: FingoCaptureCallback? = null
    ): Pair<FingoErrorCode, ByteArray?> {
        var captureTimeout = timeoutInMillis
        if(captureTimeout < FingoFactory.Constants.TEN_SECS){
            captureTimeout = DEFAULT_TIMEOUT
        }

        //TODO HANDLE RESTING

        val h1Client = getH1Client()
        if (h1Client == null) {
            Log.w(TAG, "Device inaccessible can't start capture session")
            return Pair(FingoErrorCode.H1_DEVICE_NOT_FOUND, null)
        }
        
        return try {
            captureSessionActive = true
            fingoCaptureCallback?.onCaptureStarted()
            val captureSessionData = h1Client.capture(captureTimeout.toInt())
            captureSessionActive = false
            Log.d(TAG, "Fingo capture session finished successfully")
            scannerResting = true
            Pair(FingoErrorCode.H1_OK, captureSessionData)
        }
        catch (h1ClientException: H1ClientException) {
            val fingoErrorCode = FingoErrorCode.parseErrorCode(h1ClientException.h1Error)
            if (fingoErrorCode != FingoErrorCode.H1_CANCELLED) {
                Log.e(TAG, "Fingo capture session failed: $h1ClientException")
                h1ClientException.printStackTrace()
            }
            captureSessionActive = false
            Pair(fingoErrorCode, null)
        }
    }

    fun createVerificationTemplate(capturedData: ByteArray): Pair<FingoErrorCode, String?> {
        val h1Client = getH1Client()
        if (h1Client == null) {
            Log.w(TAG, "Device inaccessible can't start capture session")
            return Pair(FingoErrorCode.H1_DEVICE_NOT_FOUND, null)
        }
        
        return try {
            val verificationTemplateBase64 = h1Client.createVerificationTemplate(capturedData)
            Log.d(TAG, "Verification template created successfully")
            Pair(FingoErrorCode.H1_OK, verificationTemplateBase64)
        } catch (e: H1ClientException) {
            Log.e(TAG, "Failed to generate verification template: $e")
            e.printStackTrace()
            Pair(FingoErrorCode.parseErrorCode(e.h1Error), null)
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Failed to generate verification template seed key not set: $e")
            e.printStackTrace()
            Pair(FingoErrorCode.H1_TEMPLATE_SEED_ERROR, null)
        }
    }

    fun createEnrolmentTemplate(capturedData: Array<ByteArray?>): Pair<FingoErrorCode, String?> {
        if (capturedData.size != 3 || listOf(*capturedData).contains(null)) {
            Log.e(TAG, "Invalid capture data")
            return Pair(FingoErrorCode.H1_INVALID_ENROLLMENT_DATA, null)
        }
        
        val h1Client = getH1Client()
        if (h1Client == null) {
            Log.w(TAG, "Device inaccessible can't start capture session")
            return Pair(FingoErrorCode.H1_DEVICE_NOT_FOUND, null)
        }
        
        return try {
            val enrolmentTemplateBase64 = h1Client.createEnrolmentTemplate(capturedData)
            Log.d(TAG, "Enrolment template created successfully")
            Pair(FingoErrorCode.H1_OK, enrolmentTemplateBase64)
        } catch (e: H1ClientException) {
            Log.e(TAG, "Failed to generate enrolment template: $e")
            e.printStackTrace()
            Pair(FingoErrorCode.parseErrorCode(e.h1Error), null)
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Failed to generate enrolment template seed key not set: $e")
            e.printStackTrace()
            Pair(FingoErrorCode.H1_TEMPLATE_SEED_ERROR, null)
        }
    }

    fun verifyTemplates(
        enrolmentTemplateBase64: String,
        verificationTemplateBase64: String,
        securityLevel: SecurityLevel
    ): Pair<FingoErrorCode, Boolean?> {
        val h1Client = getH1Client()
        if (h1Client == null) {
            Log.w(TAG, "Device inaccessible can't start capture session")
            return Pair(FingoErrorCode.H1_DEVICE_NOT_FOUND, null)
        }

        return try {
            val verificationResult =
                h1Client.verify(enrolmentTemplateBase64, verificationTemplateBase64, securityLevel)
            Log.d(TAG, "Template verification finished successfully with result: $verificationResult")
            Pair(FingoErrorCode.H1_OK, verificationResult)
        } catch (e: H1ClientException) {
            Log.e(TAG, "Failed to verify templates: $e")
            e.printStackTrace()
            Pair(FingoErrorCode.parseErrorCode(e.h1Error), null)
        }
    }

    fun cancelCaptureSession(): FingoErrorCode {
        if (!captureSessionActive) {
            return FingoErrorCode.H1_OK
        }
        
        captureSessionActive = false
        val h1Client = getH1Client()
        if (h1Client == null) {
            Log.w(TAG, "Device inaccessible can't cancel capture session")
            return FingoErrorCode.H1_DEVICE_NOT_FOUND
        }
        
        return try {
            h1Client.cancel()
            Log.d(TAG, "Capture session cancelled successfully")
            FingoErrorCode.H1_OK
        } catch (e: H1ClientException) {
            Log.e(TAG, "Failed to cancel capture session")
            e.printStackTrace()
            FingoErrorCode.parseErrorCode(e.h1Error)
        }
    }

    fun closeDevice(): FingoErrorCode {
        val h1Client = fingoDevice.h1Client
        return try {
            h1Client?.closeDevice() ?: kotlin.run {
                Log.e(TAG, "closeDevice: Failed to close device NULL")
            }
            fingoDevice.initialize()
            Log.d(TAG, "Fingo device closed successfully")
            FingoErrorCode.H1_OK
        } catch (e: H1ClientException) {
            fingoDevice.initialize()
            Log.e(TAG, "Failed to close Fingo device: $e")
            e.printStackTrace()
            FingoErrorCode.parseErrorCode(e.h1Error)
        }
    }

    @Subscribe
    fun onPermissionGranted(fingoDevice: FingoDevice) {
        synchronized(this) {
            if (fingoDevice.isUsagePermissionGranted && fingoDevice.isFingoDevice) {
                val openDeviceErrorCode = openDevice()
                Log.d(TAG, "Device opened status: ${openDeviceErrorCode.name}")
            }
        }
    }

    @Subscribe
    fun onDeviceDetached(deviceDetachedEvent: DeviceDetachedEvent) {
        synchronized(this) {
            val cancelErrorCode = cancelCaptureSession()
            Log.d(TAG, "Device disconnected cancelling: " + cancelErrorCode.name)
            val closeErrorCode = closeDevice()
            Log.d(TAG, "Device disconnected: " + closeErrorCode.name)
            fingoDevice.isDeviceOpened = false
        }
    }
}