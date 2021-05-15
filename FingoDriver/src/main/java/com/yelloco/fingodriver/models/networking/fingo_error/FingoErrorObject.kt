package com.yelloco.fingodriver.models.networking.fingo_error

class FingoErrorObject
{
    var errorCode: Int? = null
    var errorMessage: String? = null

    constructor() {}
    constructor(errorCode: Int?, errorMessage: String?) {
        this.errorCode = errorCode
        this.errorMessage = errorMessage
    }

    override fun toString(): String {
        return "FingoErrorObject{" +
                "errorCode=" + errorCode +
                ", errorMessage='" + errorMessage + '\'' +
                '}'
    }
}