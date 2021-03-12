package com.yelloco.fingodriver.models.networking.payment;

public class PosData
{
    private String itemCount;
    private String location;

    public PosData(){
    }

    public PosData(String itemCount, String location) {
        this.itemCount = itemCount;
        this.location = location;
    }

    public String getItemCount() {
        return itemCount;
    }

    public void setItemCount(String itemCount) {
        this.itemCount = itemCount;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "PosData{" +
                "itemCount='" + itemCount + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}
