package com.sebastiendui.mobitimego.retrofit.GET;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Explanation of fields:
 * 1. "WelcomeScreenSeconds" - after this time main screen appears
 * 2. "RelayTimeSeconds" - for this time relay is open
 * 3. "Language" - language of app. Default is "EN"
 * 4. "CompanyName" - company name of the holder from the phone
 * 5. "SiteName" - Site name where the phone is
 * 6. "CustomMessage" - Custom message on the main screen
 * 7. "KeepAliveTime" - time in second before the app request parameter(/registerSystems)
 * 8. "SerialNumber" - serial number of the app
 * 9. "ForceRelay" - force open relay without in/out
 * 10. "AlwaysMode" - ???
 * 11. "RelayFeature" - told the application that we must use yocto-relay and not yocto power relay
 * 12. "BlockedMessage" - message if the app is blocked by the parameter "Blocked"
 * 13. "ForceUpdate" - update the application
 * 14. "Blocked" - true/false if app blocked
 * 15. "ConfScreenSeconds" - lifetime of Confirmation Screen
 * 16. "MainScreenType" - Show main screen buttons if MainScreenType == 0 then show both IN/OUT buttons;
 *      if MainScreenType == 1 then show only IN button; if MainScreenType == 2 then show only OUT button
 */
public class RegisterSystemsResponse {

    @SerializedName("WelcomeScreenSeconds")
    @Expose
    private Integer welcomeScreenSeconds;
    @SerializedName("RelayTimeSeconds")
    @Expose
    private Integer relayTimeSeconds;
    @SerializedName("Language")
    @Expose
    private String language;
    @SerializedName("CompanyName")
    @Expose
    private String companyName;
    @SerializedName("SiteName")
    @Expose
    private String siteName;
    @SerializedName("CustomMessage")
    @Expose
    private String customMessage;
    @SerializedName("KeepAliveTime")
    @Expose
    private Integer keepAliveTime;
    @SerializedName("SerialNumber")
    @Expose
    private String serialNumber;
    @SerializedName("ForceRelay")
    @Expose
    private Boolean forceRelay;
    @SerializedName("AlwaysMode")
    @Expose
    private String alwaysMode;
    @SerializedName("RelayFeature")
    @Expose
    private Boolean relayFeature;
    @SerializedName("BlockedMessage")
    @Expose
    private String blockedMessage;
    @SerializedName("ForceUpdate")
    @Expose
    private Boolean forceUpdate;
    @SerializedName("Blocked")
    @Expose
    private Boolean blocked;
    @SerializedName("ConfScreensSeconds")
    @Expose
    private Integer confScreensSeconds;
    @SerializedName("MainScreenType")
    @Expose
    private Integer mainScreenType;

    /**
     * Getters and Setters
     */
    public Integer getWelcomeScreenSeconds() {
        return welcomeScreenSeconds;
    }

    public void setWelcomeScreenSeconds(Integer welcomeScreenSeconds) {
        this.welcomeScreenSeconds = welcomeScreenSeconds;
    }

    public Integer getRelayTimeSeconds() {
        return relayTimeSeconds;
    }

    public void setRelayTimeSeconds(Integer relayTimeSeconds) {
        this.relayTimeSeconds = relayTimeSeconds;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getCustomMessage() {
        return customMessage;
    }

    public void setCustomMessage(String customMessage) {
        this.customMessage = customMessage;
    }

    public Integer getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(Integer keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Boolean getForceRelay() {
        return forceRelay;
    }

    public void setForceRelay(Boolean forceRelay) {
        this.forceRelay = forceRelay;
    }

    public String getAlwaysMode() {
        return alwaysMode;
    }

    public void setAlwaysMode(String alwaysMode) {
        this.alwaysMode = alwaysMode;
    }

    public Boolean getRelayFeature() {
        return relayFeature;
    }

    public void setRelayFeature(Boolean relayFeature) {
        this.relayFeature = relayFeature;
    }

    public String getBlockedMessage() {
        return blockedMessage;
    }

    public void setBlockedMessage(String blockedMessage) {
        this.blockedMessage = blockedMessage;
    }

    public Boolean getForceUpdate() {
        return forceUpdate;
    }

    public void setForceUpdate(Boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    public Boolean getBlocked() {
        return blocked;
    }

    public void setBlocked(Boolean blocked) {
        this.blocked = blocked;
    }

    public void setConfScreensSeconds(Integer confScreensSeconds) {
        this.confScreensSeconds = confScreensSeconds;
    }

    public Integer getConfScreensSeconds() {

        return confScreensSeconds;
    }

    public Integer getMainScreenType() {
        return mainScreenType;
    }

    public void setMainScreenType(Integer mainScreenType) {
        this.mainScreenType = mainScreenType;
    }
}
