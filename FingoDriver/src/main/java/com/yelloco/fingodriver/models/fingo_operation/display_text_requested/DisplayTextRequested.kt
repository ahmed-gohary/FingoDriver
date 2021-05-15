package com.yelloco.fingodriver.models.fingo_operation.display_text_requested

import android.content.Context
import com.yelloco.fingodriver.enums.FingoErrorCode

class DisplayTextRequested {
    var text: String? = null
    var code: String? = null
    var type: Type? = null
        private set

    constructor() {}
    constructor(text: String?) {
        this.text = text
        type = Type.TEXT
    }

    constructor(context: Context, displayMsgCode: DisplayMsgCode) {
        text = context.getString(displayMsgCode.msgResId)
        this.code = displayMsgCode.msgCode
        type = Type.MSG
    }

    constructor(context: Context, fingoErrorCode: FingoErrorCode) {
        text = context.getString(fingoErrorCode.descriptionResId)
        this.code = fingoErrorCode.errorCode.toString()
        type = Type.MSG
    }

    override fun toString(): String {
        return "DisplayTextRequested{" +
                "text='" + text + '\'' +
                ", code='" + code + '\'' +
                ", type=" + type +
                '}'
    }

    enum class Type {
        TEXT, MSG
    }
}