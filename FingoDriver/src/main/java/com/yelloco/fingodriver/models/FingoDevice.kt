package com.yelloco.fingodriver.models

import com.hitachi.fv.android.h1client.H1Client
import com.yelloco.fingodriver.FingoFactory

class FingoDevice {
    var manufacturerName: String? = null
    private var productName: String? = null
    var version: String? = null
    var serialNumber: String? = null
    var h1Client: H1Client? = null
    var isUsagePermissionGranted = false
    var isDeviceOpened = false
    var isFingoDevice = false
        private set

    constructor() {
        initialize()
    }

    constructor(
        manufacturerName: String?,
        productName: String?,
        version: String?,
        serialNumber: String?,
        h1Client: H1Client?,
        usagePermissionGranted: Boolean,
        deviceOpened: Boolean
    ) {
        this.manufacturerName = manufacturerName
        this.productName = productName
        this.version = version
        this.serialNumber = serialNumber
        this.h1Client = h1Client
        this.isUsagePermissionGranted = usagePermissionGranted
        this.isDeviceOpened = deviceOpened
        this.isFingoDevice = FingoFactory.Constants.FINGO_DEVICE_NAME == this.productName
    }

    fun getProductName(): String? {
        return productName
    }

    fun setProductName(productName: String?) {
        this.productName = productName
        isFingoDevice = FingoFactory.Constants.FINGO_DEVICE_NAME == this.productName
    }

    fun initialize() {
        manufacturerName = "FingoCo"
        productName = "Fingo Vein ID"
        version = "UNKNOWN"
        serialNumber = "UNKNOWN"
        h1Client = H1Client()
        isUsagePermissionGranted = false
        isDeviceOpened = false
        isFingoDevice = false
    }
}