package com.sebastiendui.mobitimego.retrofit.GET;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Explanation of fields:
 * 1. "HWADDR" - IMEI from device
 * 2. "Latitude" - GPS Latitude from device
 * 3. "Longitude" - GPS Longitude from device
 * 4. "VersionNumber" - Version of App
 * 5. "LightSensor" - ???
 */
public class RegisterSystemsBody {

    @SerializedName("HWADDR")
    @Expose
    private String hWADDR;
    @SerializedName("Latitude")
    @Expose
    private String latitude;
    @SerializedName("Longitude")
    @Expose
    private String longitude;
    @SerializedName("VersionNumber")
    @Expose
    private String versionNumber;
    @SerializedName("LightSensor")
    @Expose
    private String lightSensor;

    /**
     * Getters and Setters
     */
    public String getHWADDR() {
        return hWADDR;
    }

    public void setHWADDR(String hWADDR) {
        this.hWADDR = hWADDR;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getLightSensor() {
        return lightSensor;
    }

    public void setLightSensor(String lightSensor) {
        this.lightSensor = lightSensor;
    }
}
