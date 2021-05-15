package com.yelloco.fingodriver.enums

import android.util.Log
import com.yelloco.fingodriver.R

enum class FingoErrorCode(val errorCode: Int, val descriptionResId: Int)
{
    H1_USB_NOT_SUPPORTED(
        -1000,
        R.string.fingo_error_usb_not_supported
    ),
    H1_DRIVER_INITIALIZED(
        10,
        R.string.fingo_error_driver_initialized
    ),
    H1_OK(
        0,
        R.string.fingo_error_successful
    ),
    H1_CANCELLED(
        1,
        R.string.fingo_error_cancelled
    ),
    H1_DEVICE_NOT_FOUND(
        -1,
        R.string.fingo_error_device_not_found
    ),
    H1_TIMEOUT(
        -3,
        R.string.fingo_error_timeout
    ),
    H1_CAPTURE_FAIL(
        -4,
        R.string.fingo_error_capture_failed
    ),
    H1_NOT_CAPTURING(
        -6,
        R.string.fingo_error_not_capturing
    ),
    H1_UNEXPECTED(
        -10,
        R.string.fingo_error_unexpected
    ),
    H1_INTERFACE_ERROR(
        -11,
        R.string.fingo_error_interface_error
    ),
    H1_CRYPT_ERROR(
        -12,
        R.string.fingo_error_crypto_error
    ),
    H1_MEMORY_ERROR(
        -13,
        R.string.fingo_error_memory_error
    ),
    H1_INVALID_PARAMETER(
        -14,
        R.string.fingo_error_invalid_parameter
    ),
    H1_DEVICE_NOT_READY(
        -15,
        R.string.fingo_error_device_not_ready
    ),
    H1_DEVICE_ALREADY_OPENED(
        -16,
        R.string.fingo_error_device_already_opened
    ),
    H1_TEMPLATE_SEED_ERROR(
        -17,
        R.string.fingo_error_template_seed_error
    ),
    H1_INVALID_ENROLLMENT_DATA(
        -18,
        R.string.fingo_error_invalid_enrollment_data_error
    ),
    H1_INVALID_CLOUD_URL(
        -19,
        R.string.fingo_error_invalid_cloud_url
    ),
    H1_INVALID_PARTNER_ID(
        -20,
        R.string.fingo_error_invalid_partner_id
    ),
    H1_INVALID_MERCHANT_ID(
        -21,
        R.string.fingo_error_invalid_merchant_id
    ),
    H1_INVALID_TERMINAL_ID(
        -22,
        R.string.fingo_error_invalid_terminal_id
    ),
    H1_INVALID_API_KEY(
        -23,
        R.string.fingo_error_invalid_api_key
    ),
    H1_INVALID_TEMPLATE_KEY(
        -24,
        R.string.fingo_error_invalid_template_key
    ),
    H1_DEVICE_DISCONNECTED_DURING_SCANNING(
        33,
        R.string.fingo_error_device_disconnected_during_scanning
    ),
    H1_UNKNOWN_ERROR(
        Int.MIN_VALUE,
        R.string.fingo_error_unknown_error
    ),
    H1_INTERNET_PERMISSION_NOT_GRANTED(
        Int.MIN_VALUE + 1,
        R.string.fingo_error_network_permission_error
    ),
    H1_ONLINE_IDENTIFICATION_ERROR(
        Int.MIN_VALUE + 2,
        R.string.fingo_error_online_identification_error
    ),
    H1_ONLINE_ENROLLMENT_ERROR(
        Int.MIN_VALUE + 3,
        R.string.fingo_error_online_enrollment_error
    ),
    H1_ONLINE_PAYMENT_ERROR(
        Int.MIN_VALUE + 4,
        R.string.fingo_error_online_payment_error
    ),
    H1_ONLINE_REFUND_ERROR(
        Int.MIN_VALUE + 5,
        R.string.fingo_error_online_refund_error
    ),
    H1_SDK_INIT_FAILED_BLOCKED(
        Int.MIN_VALUE + 6,
        R.string.fingo_error_sdk_init_failed_can_not_proceed
    ),
    H1_SDK_PARAMS_NOT_SET(
        Int.MIN_VALUE + 7,
        R.string.fingo_error_params_not_set
    );

    companion object {
        @JvmStatic
        fun parseErrorCode(errorCode: Int): FingoErrorCode {
            for (fingoErrorCode in values()) {
                if (fingoErrorCode.errorCode == errorCode) return fingoErrorCode
            }
            Log.e("FingoErrorCode", "UNABLE TO PARSE ERROR CODE: $errorCode")
            return H1_UNKNOWN_ERROR
        }
    }
}