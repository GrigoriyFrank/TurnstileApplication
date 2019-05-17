package com.sebastiendui.mobitimego.retrofit.GET;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Explanation of fields:
 * 1. "SerialNumber" - IMEI of the device
 * 2. "DataSource" - the type of card read. Can be NFC/QR/1D
 * 3. "Direction" - IN/OUT is the type of button the user tap
 * 4. "DataRead" - data read by device
 * 5. "RFIDRef" - data entered by the user in RFID badgeKeypad screen
 * 6. "Date" - date time in format "2018-08-28T07:31:19.176"
 */
public class RegisterTeamBody {

    @SerializedName("SerialNumber")
    @Expose
    private String serialNumber;
    @SerializedName("DataSource")
    @Expose
    private String dataSource;
    @SerializedName("Direction")
    @Expose
    private String direction;
    @SerializedName("DataRead")
    @Expose
    private String dataRead;
    @SerializedName("RFIDRef")
    @Expose
    private String rFIDRef;
    @SerializedName("Date")
    @Expose
    private String date;

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getDataRead() {
        return dataRead;
    }

    public void setDataRead(String dataRead) {
        this.dataRead = dataRead;
    }

    public String getRFIDRef() {
        return rFIDRef;
    }

    public void setRFIDRef(String rFIDRef) {
        this.rFIDRef = rFIDRef;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

  //  String string = getSerialNumber() + getDataSource() + getDirection() + getDataRead()
}
