package com.yelloco.fingodriver.models.networking.fingo_error

import java.util.*

class FingoErrorResponse
{
    var fingoErrorList: List<FingoErrorObject>

    constructor() {
        fingoErrorList = ArrayList()
    }

    constructor(fingoErrorList: List<FingoErrorObject>) {
        this.fingoErrorList = fingoErrorList
    }

    override fun toString(): String {
        return "FingoErrorResponse(fingoErrorList=$fingoErrorList)"
    }


}