package com.yelloco.fingodriver.models.fingo_operation.display_text_requested;

import com.yelloco.fingodriver.R;

public enum DisplayMsgCode
{
    // choosen offset is 2000 cause H1 error codes offset is 1000
    PLEASE_INSERT_FINGER("2000", R.string.please_insert_finger),
    IDENTIFYING_PLEASE_WAIT("2001", R.string.indentifying_vein_please_wait),
    ENROLL_FIRST_SCAN_SUCCESS("2002", R.string.enrol_first_scan_success),
    ENROLL_SECOND_SCAN_SUCCESS("2003", R.string.enrol_first_scan_success),
    ENROLL_THIRD_SCAN_SUCCESS("2004", R.string.enrol_first_scan_success),
    ENROLL_TEMPLATE_SUCCESS("2005", R.string.enrollment_template_generated_successfully),
    ONLINE_VEIN_IDENTIFY_SUCCESS("2006", R.string.finger_vein_validation_success),
    ONLINE_VEIN_ENROLL_SUCCESS("2007", R.string.finger_vein_enrollment_success),
    PAYMENT_DECLINED("2008", R.string.payment_declined),
    PAYMENT_ACCEPTED("2009", R.string.payment_accepted),
    REFUND_DECLINED("2010", R.string.refund_declined),
    REFUND_ACCEPTED("2011", R.string.refund_accepted),
    CANCELLED("2012", R.string.operation_cancelled),

    ;


    private String msgCode;
    private int msgResId;

    DisplayMsgCode(String msgCode, int msgResId){
        this.msgCode = msgCode;
        this.msgResId = msgResId;
    }

    public String getMsgCode() {
        return msgCode;
    }

    public int getMsgResId() {
        return msgResId;
    }
}
