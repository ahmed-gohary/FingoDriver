package com.yelloco.fingodriver.enums;

import android.util.Log;

import com.yelloco.fingodriver.R;


public enum FingoErrorCode
{
    H1_USB_NOT_SUPPORTED(-1000, R.string.fingo_error_usb_not_supported),
    H1_DRIVER_INITIALIZED(10, R.string.fingo_error_driver_initialized),
    H1_OK(0, R.string.fingo_error_successful),
    H1_CANCELLED(1, R.string.fingo_error_cancelled),
    H1_DEVICE_NOT_FOUND(-1, R.string.fingo_error_device_not_found),
    H1_H1_TIMEOUT(-3, R.string.fingo_error_timeout),
    H1_CAPTURE_FAIL(-4, R.string.fingo_error_capture_failed),
    H1_NOT_CAPTURING(-6, R.string.fingo_error_not_capturing),
    H1_UNEXPECTED(-10, R.string.fingo_error_unexpected),
    H1_INTERFACE_ERROR(-11,R.string.fingo_error_interface_error),
    H1_CRYPT_ERROR(-12, R.string.fingo_error_crypto_error),
    H1_MEMORY_ERROR(-13, R.string.fingo_error_memory_error),
    H1_INVALID_PARAMETER(-14, R.string.fingo_error_invalid_parameter),
    H1_DEVICE_NOT_READY(-15, R.string.fingo_error_device_not_ready),
    H1_DEVICE_ALREADY_OPENED(-16, R.string.fingo_error_device_already_opened),
    H1_TEMPLATE_SEED_ERROR(-17, R.string.fingo_error_template_seed_error),
    H1_INVALID_ENROLLMENT_DATA(-18, R.string.fingo_error_invalid_enrollment_data_error),
    H1_UNKNOWN_ERROR(Integer.MIN_VALUE, R.string.fingo_error_unknown_error),
    H1_INTERNET_PERMISSION_NOT_GRANTED(Integer.MIN_VALUE + 1, R.string.fingo_error_network_permission_error),
    H1_ONLINE_IDENTIFICATION_ERROR(Integer.MIN_VALUE + 2, R.string.fingo_error_online_identification_error),
    H1_ONLINE_ENROLLMENT_ERROR(Integer.MIN_VALUE + 3, R.string.fingo_error_online_enrollment_error),
    H1_ONLINE_PAYMENT_ERROR(Integer.MIN_VALUE + 4, R.string.fingo_error_online_payment_error),
    H1_ONLINE_REFUND_ERROR(Integer.MIN_VALUE + 5, R.string.fingo_error_online_refund_error);

    private final int errorCode;
    private final int descriptionResId;

    FingoErrorCode(int errorCode, int description){
        this.errorCode = errorCode;
        this.descriptionResId = description;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public int getDescriptionResId() {
        return descriptionResId;
    }

    public static FingoErrorCode parseErrorCode(int errorCode){
        for (FingoErrorCode fingoErrorCode :FingoErrorCode.values()) {
            if(fingoErrorCode.getErrorCode() == errorCode)
                return fingoErrorCode;
        }
        Log.e("FingoErrorCode", "UNABLE TO PARSE ERROR CODE: " + errorCode);
        return FingoErrorCode.H1_UNKNOWN_ERROR;
    }
}
