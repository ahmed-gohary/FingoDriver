package com.yelloco.fingodriver.models.fingo_operation;

public class DisplayTextRequested
{
    private String text;
    private Type type;

    public DisplayTextRequested(){
    }

    public DisplayTextRequested(String text) {
        this.text = text;
        this.type = Type.TEXT;
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
                ", type=" + type +
                '}';
    }

    public enum Type {
        TEXT,
        MSG
    }
}
