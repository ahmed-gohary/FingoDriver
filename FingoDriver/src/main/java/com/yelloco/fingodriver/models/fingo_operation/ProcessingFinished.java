package com.yelloco.fingodriver.models.fingo_operation;

import com.yelloco.fingodriver.enums.FingoErrorCode;

public class ProcessingFinished
{
    private boolean status;
    private int errorCode;
    private String text;
    private String errorName;

    public ProcessingFinished(){
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getErrorName() {
        return errorName;
    }

    public void setErrorName(String errorName) {
        this.errorName = errorName;
    }

    @Override
    public String toString() {
        return "ProcessingFinished{" +
                "status=" + status +
                ", errorCode=" + errorCode +
                ", text='" + text + '\'' +
                ", errorName='" + errorName + '\'' +
                '}';
    }
}
