package com.yelloco.fingodriver.models.networking

import android.util.Log
import com.yelloco.fingodriver.enums.StorageKey
import com.yelloco.fingodriver.utils.Storage.getString
import java.util.*

class FingoRequestHelper
{
    private val headers: MutableMap<String, String?>
    var merchantId: String? = null
        private set
    var fingoCloudBaseUrl: String? = null
        private set

    private fun fillHeaderData() {
        fillApiKey()
        fillLocation()
        fillTerminalId()
        fillPartnerId()
        fillMerchantId()
        fillCloudUrl()
        fillDriverVersion()
        fillBuildNumberVersion()

        Log.d("FingoRequestHelper", this.toString())
    }

    private fun fillApiKey() {
        // add api_key
        var api_key = getString(StorageKey.API_KEY.name)
        if (api_key != null && !api_key.startsWith("x-apikey")) {
            api_key = "x-apikey $api_key"
        }
        headers[Key.AUTHORIZATION.value] = api_key
    }

    private fun fillLocation() {
        // add location
        val location = getString(StorageKey.LOCATION.name)
        headers[Key.LOCATION.value] = location
    }

    private fun fillTerminalId() {
        // add TerminalID
        val terminalId = getString(StorageKey.TERMINAL_ID.name)
        headers[Key.TERMINAL_ID.value] = terminalId
    }

    private fun fillPartnerId() {
        // add PartnerID
        val partnerId = getString(StorageKey.PARTNER_ID.name)
        headers[Key.PARTNER_ID.value] =
            partnerId
    }

    private fun fillMerchantId() {
        merchantId = getString(StorageKey.MERCHANT_ID.name)
    }

    private fun fillCloudUrl() {
        fingoCloudBaseUrl = getString(StorageKey.FINGO_CLOUD_URL.name)
    }

    private fun fillDriverVersion() {
        headers[Key.CLOUD_DRIVER_VERSION.value] = "2.0"
    }

    private fun fillBuildNumberVersion() {
        headers[Key.BUILD_NUMBER_ASSEMBLY_VERSION.value] = "1234ABCD6789ABEF"
    }

    fun getHeaders(): Map<String, String?> {
        return headers
    }

    private enum class Key(val value: String) {
        AUTHORIZATION("Authorization"), LOCATION("x-fingopay-location"), TERMINAL_ID("x-fingopay-terminalid"), PARTNER_ID(
            "x-fingopay-partnerid"
        ),
        CLOUD_DRIVER_VERSION("x-fingo-driver-version"), BUILD_NUMBER_ASSEMBLY_VERSION("x-fingo-driver-assembly");

    }

    override fun toString(): String {
        return "FingoRequestHelper{" +
                "headers=" + headers +
                ", merchantId='" + merchantId + '\'' +
                ", fingoCloudBaseUrl='" + fingoCloudBaseUrl + '\'' +
                '}'
    }

    init {
        headers = HashMap()
        fillHeaderData()
    }
}