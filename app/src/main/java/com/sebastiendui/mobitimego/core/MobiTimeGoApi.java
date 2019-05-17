package com.sebastiendui.mobitimego.core;

import com.sebastiendui.mobitimego.retrofit.GET.RegisterSystemsResponse;
import com.sebastiendui.mobitimego.retrofit.GET.RegisterTeamResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Interface for Retrofit
 * There is a spec of REST API for "http://socket.mobitime.net:3002"
 */
public interface MobiTimeGoApi {


    /**
     * GET /registerSystems
     *
     * @param HWADDR        IMEI from device
     * @param Latitude      GPS latitude from device
     * @param Longitude     GPS longitude from device
     * @param VersionNumber Version of app
     * @param LightSensor   ???
     * @return object with data
     */

    @GET("/registerSystems")
    Call<RegisterSystemsResponse> registerSystems(@Query("HWADDR") String HWADDR,
                                                  @Query("Latitude") String Latitude,
                                                  @Query("Longitude") String Longitude,
                                                  @Query("VersionNumber") String VersionNumber,
                                                  @Query("LightSensor") String LightSensor
    );

    /**
     * GET /registerTeam
     *
     * @param SerialNumber IMEI from device
     * @param DataSource   the type of card read. Can be NFC/QR/1D
     * @param Direction    IN/OUT is the type of button the user tap
     * @param DataRead     data read by device
     * @param RFIDRef      data entered by the user in RFID badgeKeypad Screen
     * @param Date         date time in format "2018-08-28T07:31:19.176"
     * @return object with data
     */

    @GET("/registerTeam")
    Call<RegisterTeamResponse> registerTeam(@Query("SerialNumber") String SerialNumber,
                                            @Query("DataSource") String DataSource,
                                            @Query("Direction") String Direction,
                                            @Query("DataRead") String DataRead,
                                            @Query("RFIDRef") String RFIDRef,
                                            @Query("Date") String Date
    );

}