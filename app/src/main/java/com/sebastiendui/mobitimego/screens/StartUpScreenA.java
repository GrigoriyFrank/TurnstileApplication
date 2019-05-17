package com.sebastiendui.mobitimego.screens;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.crashlytics.android.Crashlytics;
import com.sebastiendui.mobitimego.R;
import com.sebastiendui.mobitimego.core.App;
import com.sebastiendui.mobitimego.core.BaseActivity;
import com.sebastiendui.mobitimego.core.MyLocationListener;
import com.sebastiendui.mobitimego.retrofit.GET.RegisterSystemsResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Class for StartUp Screen:
 * 1. Loading Screen
 * 2. Deactivated Screen
 * 3. Welcome Screen
 * Representation of Screen depends on parameters from GET /registerSystems output:
 * "Blocked", "WelcomeScreenSeconds".
 * Also it depends on the internet connection, when lost - text with "No Internet Connection" appears.
 */
public class StartUpScreenA extends BaseActivity {

    private static final String TAG = "StartUpScreenA";
    private static final int PERMISSION_REQUESTS = 1;

    static final int REQUEST_INSTALL = 1;
    static final int REQUEST_UNINSTALL = 2;

    private TextView loadingTextView;
    private String[] latLongStrings;

    /*
     * This Handler is used to run tasks periodically
     */
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_up_screen_a);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {

//            Toast.makeText(this, "No flash", Toast.LENGTH_SHORT).show();
        }

        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo("com.sebastiendui.mobitimego", 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        int curVersionCode = 0;
        int curInstallLocation = 0;

        if (packageInfo != null) {
            curVersionCode = packageInfo.versionCode;
            curInstallLocation = packageInfo.installLocation;
        }

        Log.i(TAG, "versionCode = " + curVersionCode + " installLocation = " + curInstallLocation);

        loadingTextView = findViewById(R.id.loading_textview);

        Log.i(TAG, " getExternalStorageState() = " + Environment.getExternalStorageState());
        Log.i(TAG, " getExternalStorageDirectory() = " + Environment.getExternalStorageDirectory());
        Log.i(TAG, " getExternalStorageDirectory().getAbsolutePath() = " + Environment.getExternalStorageDirectory().getAbsolutePath());


        if (allPermissionsGranted()) {

            getLocation();
            registerSystemsResponse();


        } else {
            getRuntimePermissions();

        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_INSTALL) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Install succeeded!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Install canceled!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Install Failed!", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_UNINSTALL) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Uninstall succeeded!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Uninstall canceled!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Uninstall Failed!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Runnable backActivityInForeground = new Runnable() {
        public void run() {
            Intent backActivityInForeground = new Intent(getApplicationContext(), StartUpScreenA.class);
            backActivityInForeground.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(backActivityInForeground);
            Log.i(TAG, " backActivityInForeground... ");
        }
    };

    /**
     * This runnable is for reconnecting to the server
     */
    private Runnable reconnect = new Runnable() {
        public void run() {

            registerSystemsResponse();
            Log.i(TAG, " reconnect... ");
        }
    };

    /**
     * This runnable is for start MainScreen after specified time
     */
    private Runnable delayedStartOfMainScreenB = new Runnable() {
        public void run() {
            Intent startMainScreenB = new Intent(getApplicationContext(), MainScreenB.class);
            startActivity(startMainScreenB);
            Log.i(TAG, " delayedStartOfMainScreenB... ");
        }
    };


    /**
     * GET /registerSystems implementation
     */
    private void registerSystemsResponse() {


        App.getApi().registerSystems(getIMEI(), latLongStrings[0], latLongStrings[1], "1.0", "60")
                .enqueue(new Callback<RegisterSystemsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<RegisterSystemsResponse> call, @NonNull Response<RegisterSystemsResponse> response) {

                        if (response.isSuccessful() && response.body() != null) {
                            askForBlocked(response.body());

                        } else {

//                            Toast.makeText(StartUpScreenA.this, String.format("Response is %s. Reconnecting...", String.valueOf(response.code())), Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Can't receive response");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<RegisterSystemsResponse> call, @NonNull Throwable t) {

//                        Toast.makeText(StartUpScreenA.this, "An error occurred during networking " + t.toString(), Toast.LENGTH_SHORT).show();
                        Log.e("TestRequestResults", "onFailure(): " + t.toString());

                        mHandler.removeCallbacks(reconnect);
                        mHandler.postDelayed(reconnect, 10000);
                        loadingTextView.setText(getString(R.string.no_internet_connection));

                    }
                });


    }


    /**
     * Get location via GPS
     */

    @SuppressLint("MissingPermission")
    public void getLocation() {

        LocationManager locationManager;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        MyLocationListener myLocationListener = new MyLocationListener();

        Location location;

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        String provider = locationManager.getBestProvider(criteria, true);

        location = locationManager.getLastKnownLocation(provider);
        myLocationListener.onLocationChanged(location);
        latLongStrings = myLocationListener.getLatLongStrings();
        locationManager.requestLocationUpdates(provider, 2000, 10,
                myLocationListener);

    }

    /**
     * When response from server is received this method is used to refresh the screen
     *
     * @param system response from server
     */
    private void askForBlocked(RegisterSystemsResponse system) {

        if (system == null) {
            return;
        }
        if (system.getBlocked()) {


            loadingTextView.setText(getString(R.string.system_deactivated));

            mHandler.removeCallbacks(reconnect);
            mHandler.postDelayed(reconnect, (long) system.getKeepAliveTime() * 1000);

        } else {

            loadingTextView.setText(getString(R.string.welcome));
            mHandler.removeCallbacks(reconnect);
            mHandler.removeCallbacks(delayedStartOfMainScreenB);
            mHandler.postDelayed(delayedStartOfMainScreenB, (long) system.getWelcomeScreenSeconds() * 1000);

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, " onStop()");

    }

    /**
     * Get IMEI of phone.
     *
     * @return IMEI of phone
     * If there is no IMEI on phone return "357374089317919"
     * If TelefonyManager instance == null return empty IMEI
     */

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public String getIMEI() {

        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = "";
        if (telephonyManager != null) {

            imei = telephonyManager.getDeviceId();
            //imei = telephonyManager.getImei();

            if (imei == null) {

                return "357374089317919";
            }

        }

        return imei;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(reconnect);
        mHandler.removeCallbacks(delayedStartOfMainScreenB);

       // mHandler.removeCallbacks(backActivityInForeground);
       // mHandler.postDelayed(backActivityInForeground, 10000);


    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.post(reconnect);
        mHandler.postDelayed(reconnect, 10000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(reconnect);
        mHandler.removeCallbacks(delayedStartOfMainScreenB);
    }

    /**
     * Methods for requesting permissions:
     * {@link #getRequiredPermissions},
     * {@Link #allPermissionsGranted},
     * {@Link #getRuntimePermissions},
     * {@Link #onRequestPermissionsResult(int, String[], int[])},
     * {@Link #isPermissionGranted(Context, String)}
     */
    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "Permission granted!!!!!! onRequestPermissionsResult()");
        if (allPermissionsGranted()) {

            getLocation();
            registerSystemsResponse();

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }


}