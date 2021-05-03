package com.yelloco.fingodriver.models.networking.identify;

public class IdentifyRequest
{
    public String verificationTemplate;

    public IdentifyRequest(){
    }

    public IdentifyRequest(String verificationTemplate) {
        this.verificationTemplate = verificationTemplate;
    }

    public String getVerificationTemplate() {
        return verificationTemplate;
    }

    public void setVerificationTemplate(String verificationTemplate) {
        this.verificationTemplate = verificationTemplate;
    }

    @Override
    public String toString() {
        return "IdentifyRequest{" +
                "verificationTemplate='" + verificationTemplate.substring(0, 10) + '\'' +
                '}';
    }
}
