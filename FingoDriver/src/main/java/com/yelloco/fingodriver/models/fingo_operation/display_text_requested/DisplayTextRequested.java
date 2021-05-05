package com.yelloco.fingodriver.models.fingo_operation.display_text_requested;

import android.content.Context;

import com.yelloco.fingodriver.enums.FingoErrorCode;

public class DisplayTextRequested
{
    private String text;
    private String code;
    private Type type;

    public DisplayTextRequested(){
    }

    public DisplayTextRequested(String text) {
        this.text = text;
        this.type = Type.TEXT;
    }

    public DisplayTextRequested(Context context, DisplayMsgCode displayMsgCode){
        this.text = context.getString(displayMsgCode.getMsgResId());
        this.code = displayMsgCode.getMsgCode();
        this.type = Type.MSG;
    }

    public DisplayTextRequested(Context context, FingoErrorCode fingoErrorCode){
        this.text = context.getString(fingoErrorCode.getDescriptionResId());
        this.code = String.valueOf(fingoErrorCode.getErrorCode());
        this.type = Type.MSG;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "DisplayTextRequested{" +
                "text='" + text + '\'' +
                ", code='" + code + '\'' +
                ", type=" + type +
                '}';
    }

    public enum Type {
        TEXT,
        MSG
    }
}
