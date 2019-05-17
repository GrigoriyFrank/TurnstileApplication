package com.sebastiendui.mobitimego.retrofit.GET;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Explanation of fields:
 * 1. "InssValid" - true/false value determine if inss is valid or not
 * 2. "Name" - name of the user scanned
 * 3. "Company" - company name of the user scanned
 * 4. "ErrorMessage" - error message if data not read correctly
 * 5. "Access" - ???
 * 6. "INSS" - message to show in bottom right in OK/KO screens
 */
public class RegisterTeamResponse {

    @SerializedName("InssValid")
    @Expose
    private Boolean inssValid;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("Company")
    @Expose
    private String company;
    @SerializedName("ErrorMessage")
    @Expose
    private String errorMessage;
    @SerializedName("Access")
    @Expose
    private String access;
    @SerializedName("INSS")
    @Expose
    private String iNSS;

    /**
     * Getters and Setters
     */
    public Boolean getInssValid() {
        return inssValid;
    }

    public void setInssValid(Boolean inssValid) {
        this.inssValid = inssValid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public String getINSS() {
        return iNSS;
    }

    public void setINSS(String iNSS) {
        this.iNSS = iNSS;
    }
}
