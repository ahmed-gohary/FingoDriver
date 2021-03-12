package com.yelloco.fingodriver.models.networking.refund;

public class TerminalData
{
    private String location;

    public TerminalData(){
    }

    public TerminalData(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "TerminalData{" +
                "location='" + location + '\'' +
                '}';
    }
}
