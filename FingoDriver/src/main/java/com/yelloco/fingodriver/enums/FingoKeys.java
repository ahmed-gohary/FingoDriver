package com.yelloco.fingodriver.enums;

public enum FingoKeys
{
    FINGO_TEMPLATE_KEY_SEED("FvCoreSample1"),
    FINGO_API_KEY("1761900a-bc4b-4406-a0e4-eae4df1a38cd"),
    FINGO_MERCHANT_ID("1dd56035-d914-44bb-b806-3b85f714fa91"),
    FINGO_CLOUD_BASE_URL("https://sandbox.fingo.to/api/");

    private String value;
    FingoKeys(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
