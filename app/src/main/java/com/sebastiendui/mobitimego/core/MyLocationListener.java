package com.sebastiendui.mobitimego.core;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

/**
 * This class is for listening of location changes
 */

public class MyLocationListener implements LocationListener {

    private String[] latLongStrings = new String[2];
    private final static String TAG = "MyLocationListener";



    @Override
    public void onLocationChanged(Location location) {


            if (location != null) {

                double lat = location.getLatitude();
                double lng = location.getLongitude();
                latLongStrings[0] = "" + lat;
                latLongStrings[1] = "" + lng;

                Log.i(TAG, "If location is not null:" + "\n" + "Latitude = " + latLongStrings[0] + "\n"
                        + " Longitude = " + latLongStrings[1]);
            } else {


                double lat = 0;
                double lng = 0;
                latLongStrings[0] = "" + lat;
                latLongStrings[1] = "" + lng;

                Log.i(TAG, "If location is null:" + "\n" + "Latitude = " + latLongStrings[0] + "\n"
                        + " Longitude = " + latLongStrings[1]);
            }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public String[] getLatLongStrings() {
        return latLongStrings;
    }
}
