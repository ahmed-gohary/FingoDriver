package com.yelloco.fingodriver.models.fingo_operation;

public class IdentifyData
{
    private String verificationTemplate;
    private String enrolmentTemplate;
    private String veinId;
    private String memberId;
    private boolean isOnlineData;

    public IdentifyData(){
    }

    public String getVerificationTemplate() {
        return verificationTemplate;
    }

    public void setVerificationTemplate(String verificationTemplate) {
        this.verificationTemplate = verificationTemplate;
    }

    public String getEnrolmentTemplate() {
        return enrolmentTemplate;
    }

    public void setEnrolmentTemplate(String enrolmentTemplate) {
        this.enrolmentTemplate = enrolmentTemplate;
    }

    public String getVeinId() {
        return veinId;
    }

    public void setVeinId(String veinId) {
        this.veinId = veinId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public boolean isOnlineData() {
        return isOnlineData;
    }

    public void setOnlineData(boolean onlineData) {
        isOnlineData = onlineData;
    }

    @Override
    public String toString() {
        return "ResponseData{" +
                "verificationTemplate='" + verificationTemplate + '\'' +
                ", enrolmentTemplate='" + enrolmentTemplate + '\'' +
                ", veinId='" + veinId + '\'' +
                ", memberId='" + memberId + '\'' +
                ", isOnlineData=" + isOnlineData +
                '}';
    }
}
