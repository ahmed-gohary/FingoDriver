package com.yelloco.fingodriver;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;

import com.hitachi.fv.android.h1client.CryptoAlg;
import com.hitachi.fv.android.h1client.H1Client;
import com.hitachi.fv.android.h1client.H1ClientException;
import com.hitachi.fv.android.h1client.SecurityLevel;
import com.yelloco.fingodriver.enums.StorageKey;
import com.yelloco.fingodriver.models.FingoDevice;
import com.yelloco.fingodriver.callbacks.FingoCaptureCallback;
import com.yelloco.fingodriver.models.events.DeviceDetachedEvent;
import com.yelloco.fingodriver.enums.FingoErrorCode;
import com.yelloco.fingodriver.utils.Storage;
import com.yelloco.fingodriver.utils.UsbReceiver;

import org.greenrobot.eventbus.Subscribe;

import java.util.Arrays;

public class FingoPayDriver
{
    // Constants
    private static final String TAG = FingoPayDriver.class.getSimpleName();
    private static final int DEFAULT_TIMEOUT = 10000;
    private final int CONSECUTIVE_SCAN_RESTING_INTERVAL;
    private final Object lock = new Object();
    private static FingoPayDriver fingoPayDriver = new FingoPayDriver();

    // Members
    private FingoDevice fingoDevice;
    private boolean scannerResting;       // boolean to indicate wait between consecutive scans
    private boolean captureSessionActive;
    private boolean templateKeySeedSet;
    private boolean isLockActive;

    private FingoPayDriver(){
        this.fingoDevice = new FingoDevice();
        CONSECUTIVE_SCAN_RESTING_INTERVAL = Storage.getInstance().getInt(StorageKey.CONSECUTIVE_SCAN_INTERVAL.name(), FingoConstants.ONE_SECOND);
    }

    public static FingoPayDriver getInstance() {
        if(fingoPayDriver == null){
            fingoPayDriver = new FingoPayDriver();
        }
        return fingoPayDriver;
    }

    public FingoDevice getFingoDevice() {
        return fingoDevice;
    }

    public FingoErrorCode setCryptoTemplateKey(String templateKey){
        return this.setCryptoTemplateKey(templateKey, CryptoAlg.AES256);
    }

    public FingoErrorCode setCryptoTemplateKey(String templateKey, CryptoAlg cryptoAlg){
        H1Client h1Client = getH1Client();
        if(h1Client == null){
            return FingoErrorCode.H1_DEVICE_NOT_FOUND;
        }

        try {
            h1Client.setTemplateKeySeed(templateKey.getBytes(), cryptoAlg);
            this.templateKeySeedSet = true;
            Log.d(TAG, "Template key set successfully");
            return FingoErrorCode.H1_OK;
        }
        catch (H1ClientException e) {
            this.templateKeySeedSet = false;
            Log.e(TAG, "Failed to set template key" + e);
            e.printStackTrace();
            return FingoErrorCode.parseErrorCode(e.getH1Error());
        }
    }

    public FingoErrorCode openDevice() {
        String templateKeySeed = Storage.getInstance().getString(StorageKey.TEMPLATE_KEY.name());
        FingoErrorCode templateKeySeedErrorCode = this.setCryptoTemplateKey(templateKeySeed);;
        if(! FingoErrorCode.H1_OK.equals(templateKeySeedErrorCode)){
            Log.w(TAG, "Fingo Template Key Not set");
            return templateKeySeedErrorCode;
        }

        H1Client h1Client = getH1Client();
        if(h1Client == null){
            return FingoErrorCode.H1_DEVICE_NOT_FOUND;
        }
        try {
            UsbManager usbManager = UsbReceiver.getUsbManager();
            UsbDevice usbDevice = h1Client.getDevice(usbManager);
            h1Client.openDevice(usbManager, usbDevice);
            this.fingoDevice.setDeviceOpened(true);
            Log.d(TAG, "Fingo device opened successfully");
            return FingoErrorCode.H1_OK;
        }
        catch (H1ClientException e) {
            Log.e(TAG, "Failed to open Fingo device: " + e);
            this.fingoDevice.setDeviceOpened(false);
            e.printStackTrace();
            return FingoErrorCode.parseErrorCode(e.getH1Error());
        }
        catch (IllegalStateException illegalStateException){
            Log.e(TAG, "Failed to open Fingo device, already opened: " + illegalStateException);
            illegalStateException.printStackTrace();
            return FingoErrorCode.H1_DEVICE_ALREADY_OPENED;
        }
    }

    public Pair<FingoErrorCode, byte[]> capture(){
        return this.capture(DEFAULT_TIMEOUT, null);
    }

    public Pair<FingoErrorCode, byte[]> capture(FingoCaptureCallback fingoCaptureCallback) {
        return this.capture(DEFAULT_TIMEOUT, fingoCaptureCallback);
    }

    public Pair<FingoErrorCode, byte[]> capture(int timeoutInMillis, FingoCaptureCallback fingoCaptureCallback){
        if(scannerResting){
            try {
                // this will hold the calling thread until the scanner is ready
                Log.d(TAG, "Waiting for timeout between scans");
                synchronized (lock){
                    isLockActive = true;
                    lock.wait();
                }
                scannerResting = false;
                Log.d(TAG, "Scanner ready");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        H1Client h1Client = getH1Client();
        if(h1Client == null){
            Log.w(TAG, "Device inaccessible can't start capture session");
            return new Pair<>(FingoErrorCode.H1_DEVICE_NOT_FOUND, null);
        }

        try {
            captureSessionActive = true;
            if(fingoCaptureCallback != null){
                fingoCaptureCallback.onCaptureStarted();
            }
            byte[] captureSessionData = h1Client.capture(timeoutInMillis);
            captureSessionActive = false;
            Log.d(TAG, "Fingo capture session finished successfully");
            scannerResting = true;
            activateIntervalBetweenCaptureSessions();

            return new Pair<>(FingoErrorCode.H1_OK, captureSessionData);
        }
        catch (H1ClientException e) {
            FingoErrorCode fingoErrorCode = FingoErrorCode.parseErrorCode(e.getH1Error());
            if(! fingoErrorCode.equals(FingoErrorCode.H1_CANCELLED)){
                Log.e(TAG, "Fingo capture session failed: " + e);
                e.printStackTrace();
            }
            captureSessionActive = false;
            return new Pair<>(fingoErrorCode, null);
        }
    }

    public Pair<FingoErrorCode, String> createVerificationTemplate(byte[] capturedData){
        H1Client h1Client = getH1Client();
        if(h1Client == null){
            Log.w(TAG, "Device inaccessible can't start capture session");
            return new Pair<>(FingoErrorCode.H1_DEVICE_NOT_FOUND, null);
        }

        try {
            String verificationTemplateBase64 = h1Client.createVerificationTemplate(capturedData);
            Log.d(TAG, "Verification template created successfully");
            return new Pair<>(FingoErrorCode.H1_OK, verificationTemplateBase64);
        }
        catch (H1ClientException e) {
            Log.e(TAG, "Failed to generate verification template: " + e);
            e.printStackTrace();
            return new Pair<>(FingoErrorCode.parseErrorCode(e.getH1Error()), null);
        }
        catch (IllegalStateException e){
            Log.e(TAG, "Failed to generate verification template seed key not set: " + e);
            e.printStackTrace();
            return new Pair<>(FingoErrorCode.H1_TEMPLATE_SEED_ERROR, null);
        }
    }

    public Pair<FingoErrorCode, String> createEnrolmentTemplate(byte[][] capturedData){
        if(capturedData.length != 3 || Arrays.asList(capturedData).contains(null)){
            Log.e(TAG, "Invalid capture data");
            return new Pair<>(FingoErrorCode.H1_INVALID_ENROLLMENT_DATA, null);
        }
        H1Client h1Client = getH1Client();
        if(h1Client == null){
            Log.w(TAG, "Device inaccessible can't start capture session");
            return new Pair<>(FingoErrorCode.H1_DEVICE_NOT_FOUND, null);
        }

        try {
            String enrolmentTemplateBase64 = h1Client.createEnrolmentTemplate(capturedData);
            Log.d(TAG, "Enrolment template created successfully");
            return new Pair<>(FingoErrorCode.H1_OK, enrolmentTemplateBase64);
        }
        catch (H1ClientException e) {
            Log.e(TAG, "Failed to generate enrolment template: " + e);
            e.printStackTrace();
            return new Pair<>(FingoErrorCode.parseErrorCode(e.getH1Error()), null);
        }
        catch (IllegalStateException e){
            Log.e(TAG, "Failed to generate enrolment template seed key not set: " + e);
            e.printStackTrace();
            return new Pair<>(FingoErrorCode.H1_TEMPLATE_SEED_ERROR, null);
        }
    }

    public Pair<FingoErrorCode, Boolean> verifyTemplates(String enrolmentTemplateBase64, String verificationTemplateBase64, SecurityLevel securityLevel){
        H1Client h1Client = getH1Client();
        if(h1Client == null){
            Log.w(TAG, "Device inaccessible can't start capture session");
            return new Pair<>(FingoErrorCode.H1_DEVICE_NOT_FOUND, null);
        }

        try {
            boolean verificationResult = h1Client.verify(enrolmentTemplateBase64, verificationTemplateBase64, securityLevel);
            Log.d(TAG, "Template verification finished successfully with result: " + verificationResult);
            return new Pair<>(FingoErrorCode.H1_OK, verificationResult);
        }
        catch (H1ClientException e) {
            Log.e(TAG, "Failed to verify templates: " + e);
            e.printStackTrace();
            return new Pair<>(FingoErrorCode.parseErrorCode(e.getH1Error()), null);
        }
    }

    public FingoErrorCode cancelCaptureSession(){
        if(! captureSessionActive){
            return FingoErrorCode.H1_OK;
        }
        captureSessionActive = false;

        H1Client h1Client = getH1Client();

        if(h1Client == null){
            Log.w(TAG, "Device inaccessible can't cancel capture session");
            return FingoErrorCode.H1_DEVICE_NOT_FOUND;
        }

        try {
            h1Client.cancel();
            Log.d(TAG, "Capture session cancelled successfully");
            return FingoErrorCode.H1_OK;
        }
        catch (H1ClientException e) {
            Log.e(TAG, "Failed to cancel capture session");
            e.printStackTrace();
            return FingoErrorCode.parseErrorCode(e.getH1Error());
        }
    }

    public FingoErrorCode closeDevice(){
        H1Client h1Client = this.fingoDevice.getH1Client();

        try {
            h1Client.closeDevice();
            FingoPayDriver.getInstance().getFingoDevice().initialize();
            Log.d(TAG, "Fingo device closed successfully");
            return FingoErrorCode.H1_OK;
        }
        catch (H1ClientException e) {
            FingoPayDriver.getInstance().getFingoDevice().initialize();
            Log.e(TAG, "Failed to close Fingo device: " + e);
            e.printStackTrace();
            return FingoErrorCode.parseErrorCode(e.getH1Error());
        }
    }

    private H1Client getH1Client(){
        return this.getFingoDevice().isUsagePermissionGranted() ? this.getFingoDevice().getH1Client() : null;
    }

    private void activateIntervalBetweenCaptureSessions(){
        new Thread(() -> {
            Log.d(TAG, "Scanner going to rest");
            SystemClock.sleep(CONSECUTIVE_SCAN_RESTING_INTERVAL);
            scannerResting = false;
            synchronized (lock){
                if(isLockActive){
                    isLockActive = false;
                    lock.notify();
                }
            }
            Log.d(TAG, "Scanner resting finished");
        }).start();
    }

    @Subscribe
    public void onPermissionGranted(FingoDevice fingoDevice){
        synchronized (this){
            if(fingoDevice.isUsagePermissionGranted() && fingoDevice.isFingoDevice()){
                openDevice();
            }
        }
    }

    @Subscribe
    public void onDeviceDetached(DeviceDetachedEvent deviceDetachedEvent){
        synchronized (this){
            FingoErrorCode cancelErrorCode = this.cancelCaptureSession();
            Log.d(TAG, "Device disconnected cancelling: " + cancelErrorCode.name());
            FingoErrorCode closeErrorCode = this.closeDevice();
            Log.d(TAG, "Device disconnected: " + closeErrorCode.name());
        }
    }
}
