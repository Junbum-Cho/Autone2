package com.example.autone;

public class User_WIFI {
    private String wifiName;
    private String wifiDetails;

    public User_WIFI() {
        // Default constructor required for calls to DataSnapshot.getValue(User_WIFI.class)
    }

    public User_WIFI(String wifiName, String wifiDetails) {
        this.wifiName = wifiName;
        this.wifiDetails = wifiDetails;
    }

    public String getWifiName() {
        return wifiName;
    }

    public void setWifiName(String wifiName) {
        this.wifiName = wifiName;
    }

    public String getWifiDetails() {
        return wifiDetails;
    }

    public void setWifiDetails(String wifiDetails) {
        this.wifiDetails = wifiDetails;
    }
}
