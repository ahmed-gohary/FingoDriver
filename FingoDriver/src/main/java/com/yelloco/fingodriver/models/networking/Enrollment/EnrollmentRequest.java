package com.yelloco.fingodriver.models.networking.Enrollment;

public class EnrollmentRequest
{
    private int hand;
    private String finger;
    private String enrolmentTemplate;
    private String verificationTemplate;

    public EnrollmentRequest() {
    }

    public EnrollmentRequest(int hand, String finger, String enrolmentTemplate, String verificationTemplate) {
        this.hand = hand;
        this.finger = finger;
        this.enrolmentTemplate = enrolmentTemplate;
        this.verificationTemplate = verificationTemplate;
    }

    public int getHand() {
        return hand;
    }

    public void setHand(int hand) {
        this.hand = hand;
    }

    public String getFinger() {
        return finger;
    }

    public void setFinger(String finger) {
        this.finger = finger;
    }

    public String getEnrolmentTemplate() {
        return enrolmentTemplate;
    }

    public void setEnrolmentTemplate(String enrolmentTemplate) {
        this.enrolmentTemplate = enrolmentTemplate;
    }

    public String getVerificationTemplate() {
        return verificationTemplate;
    }

    public void setVerificationTemplate(String verificationTemplate) {
        this.verificationTemplate = verificationTemplate;
    }

    @Override
    public String toString() {
        return "EnrollmentRequest{" +
                "hand=" + hand +
                ", finger='" + finger + '\'' +
                ", enrolmentTemplate='" + enrolmentTemplate.substring(0, 10) + '\'' +
                ", verificationTemplate='" + verificationTemplate.substring(0, 10) + '\'' +
                '}';
    }
}
