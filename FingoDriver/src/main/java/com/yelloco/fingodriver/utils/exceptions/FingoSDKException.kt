package com.yelloco.fingodriver.utils.exceptions

class FingoSDKException : Exception {
    constructor() {}
    constructor(message: String?) : super(message) {}
    constructor(message: String?, cause: Throwable?) : super(message, cause) {}
}