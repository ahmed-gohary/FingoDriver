package com.yelloco.fingodriver.utils

import android.util.Log
import com.yelloco.fingodriver.enums.FingoErrorCode
import com.yelloco.fingodriver.enums.StorageKey

class FingoParams
{
    var consecutiveScanInterval = 0
    var cloudUrl: String? = null
    var partnerId: String? = null
    var apiKey: String? = null
    var merchantId: String? = null
    var location: String? = null
    var terminalId: String? = null
    var templateKeySeed: String? = null

    constructor() {}
    constructor(
        consecutiveScanInterval: Int,
        cloudUrl: String?,
        partnerId: String?,
        apiKey: String?,
        merchantId: String?,
        location: String?,
        terminalId: String?,
        templateKeySeed: String?
    ) {
        this.consecutiveScanInterval = consecutiveScanInterval
        this.cloudUrl = cloudUrl
        this.partnerId = partnerId
        this.apiKey = apiKey
        this.merchantId = merchantId
        this.location = location
        this.terminalId = terminalId
        this.templateKeySeed = templateKeySeed
    }

    fun validate(): FingoErrorCode {
        if (cloudUrl.isNullOrEmpty()) {
            Log.e(TAG, "INVALID CLOUD URL: $cloudUrl")
            return FingoErrorCode.H1_INVALID_CLOUD_URL
        }
        if (partnerId.isNullOrEmpty()) {
            Log.e(TAG, "INVALID PARTNER ID: $partnerId")
            return FingoErrorCode.H1_INVALID_PARTNER_ID
        }
        if (merchantId.isNullOrEmpty()) {
            Log.e(TAG, "INVALID MERCHANT ID: $merchantId")
            return FingoErrorCode.H1_INVALID_MERCHANT_ID
        }
        if (terminalId.isNullOrEmpty()) {
            Log.e(TAG, "INVALID TERMINAL ID: $terminalId")
            return FingoErrorCode.H1_INVALID_TERMINAL_ID
        }
        if (apiKey.isNullOrEmpty()) {
            Log.e(TAG, "INVALID API KEY: $apiKey")
            return FingoErrorCode.H1_INVALID_API_KEY
        } else if (apiKey!!.startsWith("x-apikey")) {
            Log.e(TAG, "INVALID API KEY CAN'T START WITH \"x-apikey\": $apiKey")
            return FingoErrorCode.H1_INVALID_API_KEY
        }
        if (templateKeySeed.isNullOrEmpty()) {
            Log.e(TAG, "INVALID TEMPLATE KEY: $templateKeySeed")
            return FingoErrorCode.H1_INVALID_TEMPLATE_KEY
        }

        return FingoErrorCode.H1_OK
    }

    fun loadParams() {
        if (consecutiveScanInterval < FingoConstants.ONE_SECOND) {
            consecutiveScanInterval = FingoConstants.ONE_SECOND
        }
        Storage.storeString(StorageKey.FINGO_CLOUD_URL.name, cloudUrl)
        Storage.storeString(StorageKey.PARTNER_ID.name, partnerId)
        Storage.storeString(StorageKey.MERCHANT_ID.name, merchantId)
        Storage.storeString(StorageKey.TERMINAL_ID.name, terminalId)
        Storage.storeString(StorageKey.API_KEY.name, apiKey)
        Storage.storeString(StorageKey.TEMPLATE_KEY.name, templateKeySeed)
        Storage.storeInt(StorageKey.CONSECUTIVE_SCAN_INTERVAL.name, consecutiveScanInterval)
        Storage.storeString(StorageKey.LOCATION.name, if (location == null) "" else location)
        Storage.storeBoolean(StorageKey.PARAMS_STATUS.name, true)
    }

    companion object {
        private const val TAG = "FingoParams"
        @JvmStatic
        val fingoParams: FingoParams
            get() {
                val fingoParams = FingoParams()
                fingoParams.cloudUrl = Storage.getString(StorageKey.FINGO_CLOUD_URL.name)
                fingoParams.partnerId = Storage.getString(StorageKey.PARTNER_ID.name)
                fingoParams.merchantId = Storage.getString(StorageKey.MERCHANT_ID.name)
                fingoParams.terminalId = Storage.getString(StorageKey.TERMINAL_ID.name)
                fingoParams.apiKey = Storage.getString(StorageKey.API_KEY.name)
                fingoParams.templateKeySeed = Storage.getString(StorageKey.TEMPLATE_KEY.name)
                fingoParams.consecutiveScanInterval =
                    Storage.getInt(StorageKey.CONSECUTIVE_SCAN_INTERVAL.name, FingoConstants.ONE_SECOND)
                fingoParams.location = Storage.getString(StorageKey.LOCATION.name)
                return fingoParams
            }
get() {
        return com.yelloco.fingodriver.utils.FingoParams.storedParams
    }

        fun status(): Boolean {
            return Storage.getBoolean(StorageKey.PARAMS_STATUS.name, false)
        }
    }
}