package com.yelloco.fingodriver.models.fingo_operation

class ProcessingFinished
{
    var status = false
    var errorCode = 0
    var text: String? = null
    var errorName: String? = null

    override fun toString(): String {
        return "ProcessingFinished{" +
                "status=" + status +
                ", errorCode=" + errorCode +
                ", text='" + text + '\'' +
                ", errorName='" + errorName + '\'' +
                '}'
    }
}