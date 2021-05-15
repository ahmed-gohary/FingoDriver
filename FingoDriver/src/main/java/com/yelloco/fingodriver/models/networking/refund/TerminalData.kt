package com.yelloco.fingodriver.models.networking.refund

class TerminalData
{
    var location: String? = null

    constructor() {}
    constructor(location: String?) {
        this.location = location
    }

    override fun toString(): String {
        return "TerminalData{" +
                "location='" + location + '\'' +
                '}'
    }
}