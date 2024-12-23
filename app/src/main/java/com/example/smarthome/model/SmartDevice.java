package com.example.smarthome.model;


public class SmartDevice {
    private String name;
    private String macAddress;
    private String type; // "light" or "fan"
    private int status; // 0 = off, 1 = on

    public SmartDevice(String name, String macAddress, String type) {
        this.name = name;
        this.macAddress = macAddress;
        this.type = type;
        this.status = 0; // default off
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}