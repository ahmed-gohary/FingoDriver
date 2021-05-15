package com.yelloco.fingodriver.models.networking.payment

class PosData
{
    var itemCount: String? = null
    var location: String? = null

    constructor() {}
    constructor(itemCount: String?, location: String?) {
        this.itemCount = itemCount
        this.location = location
    }

    override fun toString(): String {
        return "PosData{" +
                "itemCount='" + itemCount + '\'' +
                ", location='" + location + '\'' +
                '}'
    }
}