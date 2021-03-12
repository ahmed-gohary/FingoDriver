package com.yelloco.fingodriver.models.fingo_operation;

public class DisplayTextRequested
{
    private String text;

    public DisplayTextRequested(){
    }

    public DisplayTextRequested(String text) {
        this.text = text;
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
                '}';
    }
}
