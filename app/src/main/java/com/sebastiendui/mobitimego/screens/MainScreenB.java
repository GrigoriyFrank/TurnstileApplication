package com.sebastiendui.mobitimego.screens;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.instacart.library.truetime.TrueTimeRx;

import com.sebastiendui.mobitimego.R;
import com.sebastiendui.mobitimego.core.App;
import com.sebastiendui.mobitimego.core.BaseActivity;
import com.sebastiendui.mobitimego.core.MyLocationListener;
import com.sebastiendui.mobitimego.core.MyPhoneStateListener;
import com.sebastiendui.mobitimego.retrofit.GET.RegisterSystemsResponse;
import com.sebastiendui.mobitimego.retrofit.GET.RegisterTeamResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Class for Main Screen
 * When the internet connection is lost, the text with "No Internet Connection" appears and IN/OUT
 * buttons become not clickable
 */
public class MainScreenB extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "MainScreenB";
    private static final boolean DEBUG = true;

    private TextView companyName;
    private TextView siteName;
    private TextView noInternetConnection;
    private TextView serialNumber;

    private ImageView signalStrength;

    private Vibrator vibrator;

    /*
     * Used for reconnecting to the server
     */
    private Handler mHandler = new Handler();

    /*
     * Used for listening battery charge level
     */
    private Handler mHandlerBattery = new Handler();

    private String[] latLongStrings;

    private RegisterSystemsResponse system = null;

    private ImageButton inImageButton;
    private ImageButton outImageButton;

    private ImageView batteryLowImageView;

    /*
     * true when battery level less then 15%
     */

    private boolean batteryLow = false;

    private BroadcastReceiver batteryChargeLevelReceiver;

    /*
     * For RelayFeature parameter in GET /registerSystems response
     */

    private boolean isRelayFeature = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen_b);

        getLocation();

        companyName = findViewById(R.id.company_name);
        siteName = findViewById(R.id.site_name);
        noInternetConnection = findViewById(R.id.no_internet_connection_main_screen);
        serialNumber = findViewById(R.id.serial_number);
        signalStrength = findViewById(R.id.signal_strength);

        inImageButton = findViewById(R.id.in_button);
        inImageButton.setOnClickListener(this);

        outImageButton = findViewById(R.id.out_button);
        outImageButton.setOnClickListener(this);

        findViewById(R.id.help_button).setOnClickListener(this);

        batteryLowImageView = findViewById(R.id.battery);

        //vibrator
        vibrator = getVibrator();

        cellSignalStrength();

        batteryChargeLevelReceiver = new BatteryReceiver();
        this.registerReceiver(batteryChargeLevelReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        registerSystemsResponse();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);

        if (getIntent().getStringExtra(getString(R.string.data_read_json)) != null) {
            sendReguestTeamMember(intent);
        } else {

            // Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
        }

        if (DEBUG) {

            Log.i(TAG, "onNewIntent(), Direction = " + getIntent().getStringExtra("Direction"));
            Log.i(TAG, "onNewIntent(), RFIDRef = " + getIntent().getStringExtra("RFIDRef"));
            Log.i(TAG, "onNewIntent(), DataRead = " + getIntent().getStringExtra("DataRead"));
            Log.i(TAG, "onNewIntent(), DataSource = " + getIntent().getStringExtra("DataSource"));
        }
    }

    /**
     * Get location via GPS
     */
    @SuppressLint("MissingPermission")
    private void getLocation() {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        MyLocationListener myLocationListener = new MyLocationListener();

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);

        String provider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(provider);


        myLocationListener.onLocationChanged(location);
        latLongStrings = myLocationListener.getLatLongStrings();
        locationManager.requestLocationUpdates(provider, 2000, 10,
                myLocationListener);
    }

    /**
     * Used for reconnecting to the server
     */
    private Runnable reconnect = () -> {

        registerSystemsResponse();

        Log.i(TAG, " reconnect... ");
    };

    /**
     * Used for refresh R image, if battery level less then 15% and Relay feature exist
     */
    private Runnable askBatteryCharge = new Runnable() {
        public void run() {

            if (isRelayFeature) {

                batteryLowImageView.setVisibility(View.VISIBLE);

                if (batteryLow) {
                    batteryLowImageView.setImageDrawable(getResources().getDrawable(R.drawable.rbarre, null));
                } else {
                    batteryLowImageView.setImageDrawable(getResources().getDrawable(R.drawable.r_, null));
                }


            } else {

                batteryLowImageView.setVisibility(View.GONE);

            }

            Log.i(TAG, " askBatteryCharge");
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

                            noInternetConnection.setVisibility(View.GONE);
                            companyName.setVisibility(View.VISIBLE);
                            siteName.setVisibility(View.VISIBLE);
                            serialNumber.setVisibility(View.VISIBLE);

                            system = response.body();

                            inImageButton.setEnabled(true);
                            outImageButton.setEnabled(true);

                            companyName.setText(system.getCompanyName());
                            siteName.setText(system.getSiteName());
                            serialNumber.setText(system.getSerialNumber());

                            if (system.getBlocked() != null) {

                                if (system.getBlocked()) {

                                    goToStartUpScreen();
                                }

                            }
                            Integer mainScreenType = system.getMainScreenType();

                            if (mainScreenType != null) {
                                setVisibilityForInOutButtons(mainScreenType);
                                Log.i(TAG, "mainScreenType = " + mainScreenType);
                            }

                            isRelayFeature = system.getRelayFeature();

                            Log.i(TAG, "isRelayFeature = " + isRelayFeature);

                            mHandler.postDelayed(reconnect, (long) system.getKeepAliveTime() * 1000);

                            mHandlerBattery.postDelayed(askBatteryCharge, 100);

                        } else {

                            //Toast.makeText(MainScreenB.this, String.format("Response is %s. Reconnecting...", String.valueOf(response.code())), Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Can't receive response");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<RegisterSystemsResponse> call, @NonNull Throwable t) {

                        // Toast.makeText(MainScreenB.this, "An error occurred during networking " + t.toString(), Toast.LENGTH_SHORT).show();
                        Log.e("TestRequestResults", "onFailure(): " + t.toString());

                        inImageButton.setEnabled(false);
                        outImageButton.setEnabled(false);

                        noInternetConnection.setVisibility(View.VISIBLE);
                        companyName.setVisibility(View.GONE);
                        siteName.setVisibility(View.GONE);
                        serialNumber.setVisibility(View.GONE);

                        mHandler.removeCallbacks(reconnect);
                        mHandler.postDelayed(reconnect, (long) 10000);

                    }
                });
    }

    /**
     * Refresh the MainScreenB
     *
     * @param mainScreenType Integer to set visibility of IN, OUT or both buttons
     */
    private void setVisibilityForInOutButtons(Integer mainScreenType) {

        switch (mainScreenType) {

            case 0:
                inImageButton.setVisibility(View.VISIBLE);
                outImageButton.setVisibility(View.VISIBLE);
                Log.i(TAG, " case 0");
                break;

            case 1:
                inImageButton.setVisibility(View.VISIBLE);
                outImageButton.setVisibility(View.GONE);
                Log.i(TAG, " case 1");
                break;

            case 2:
                inImageButton.setVisibility(View.GONE);
                outImageButton.setVisibility(View.VISIBLE);
                Log.i(TAG, " case 2");
                break;

            default:
                inImageButton.setVisibility(View.VISIBLE);
                outImageButton.setVisibility(View.VISIBLE);
                Log.i(TAG, " default");
                break;

        }
    }

    /**
     * Fire StartUpScreen when "Blocked" is true
     */
    private void goToStartUpScreen() {

        startActivity(new Intent(this, StartUpScreenA.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
    }

    /**
     * GET /registerTeam implementation
     *
     * @param intent data from Scan Screen
     */
    private void sendReguestTeamMember(Intent intent) {

        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
        String dateStr = dateFormat.format(date);

        //get TrueTime time and date (don't needed at the moment)
        //String dateStr = getTrueTime();

        App.getApi().registerTeam(getIMEI(),
                intent.getStringExtra(getString(R.string.data_source_json)),
                intent.getStringExtra(getString(R.string.direction_json)),
                intent.getStringExtra(getString(R.string.data_read_json)),
                intent.getStringExtra(getString(R.string.rfidref_json)),
                dateStr)
                .enqueue(new Callback<RegisterTeamResponse>() {
                    @Override
                    public void onResponse(Call<RegisterTeamResponse> call, Response<RegisterTeamResponse> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            RegisterTeamResponse teamMember = response.body();

                            Intent confScrn = new Intent(getApplicationContext(), ConfirmationScreen.class);

                            confScrn.putExtra(getString(R.string.direction_json), intent.getStringExtra(getString(R.string.direction_json)));
                            confScrn.putExtra(getString(R.string.relay_time_seconds_json), intent.getIntExtra(getString(R.string.relay_time_seconds_json), 0));
                            confScrn.putExtra(getString(R.string.conf_screens_seconds_json), intent.getIntExtra(getString(R.string.conf_screens_seconds_json), 0));
                            confScrn.putExtra(getString(R.string.relay_feature_json), intent.getBooleanExtra(getString(R.string.relay_feature_json), true));
                            confScrn.putExtra(getString(R.string.access_json), teamMember.getAccess());
                            confScrn.putExtra(getString(R.string.company_json), teamMember.getCompany());
                            confScrn.putExtra(getString(R.string.error_message_json), teamMember.getErrorMessage());
                            confScrn.putExtra(getString(R.string.inss_json), teamMember.getINSS());
                            confScrn.putExtra(getString(R.string.inss_valid_json), teamMember.getInssValid());
                            confScrn.putExtra(getString(R.string.name_json), teamMember.getName());

                            startActivity(confScrn);

                        } else {
//                            Toast.makeText(MainScreenB.this,
//                                    String.format("Response is %s. Reconnecting...", String.valueOf(response.code()))
//                                    , Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<RegisterTeamResponse> call, @NonNull Throwable t) {

//                        Toast.makeText(MainScreenB.this, "Can't receive response " + t.toString(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    /**
     * Fires when specified button clicked
     *
     * @param v button
     */
    @Override
    public void onClick(View v) {

        if (system == null) {

            return;
        }

        int id = v.getId();

        switch (id) {

            case R.id.in_button:

                vibrator.vibrate(100);

                Intent intentIn = new Intent(this, ScanScreen.class);
                intentIn.putExtra(getString(R.string.direction_json), "IN");
                intentIn.putExtra(
                        getString(R.string.relay_time_seconds_json),
                        system.getRelayTimeSeconds().intValue()
                );
                intentIn.putExtra(
                        getString(R.string.conf_screens_seconds_json),
                        system.getConfScreensSeconds().intValue()
                );
                intentIn.putExtra(getString(R.string.relay_feature_json), system.getRelayFeature().booleanValue());
                startActivity(intentIn);
                break;

            case R.id.out_button:
                Log.i(TAG, "out_button clicked");
                vibrator.vibrate(100);

                Intent intentOut = new Intent(this, ScanScreen.class);
                intentOut.putExtra(getString(R.string.direction_json), "OUT");
                intentOut.putExtra(
                        getString(R.string.relay_time_seconds_json),
                        system.getRelayTimeSeconds().intValue()
                );
                intentOut.putExtra(
                        getString(R.string.conf_screens_seconds_json),
                        system.getConfScreensSeconds().intValue()
                );
                intentOut.putExtra(getString(R.string.relay_feature_json), system.getRelayFeature().booleanValue());

                startActivity(intentOut);
                break;

            case R.id.help_button:

                break;

            default:
                break;


        }
    }

    /**
     * Listen to changes of network signal strength
     */
    @SuppressLint("MissingPermission")
    public void cellSignalStrength() {

        MyPhoneStateListener myPhoneStateListener = new MyPhoneStateListener(this, signalStrength);
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

        if (telephonyManager != null) {

            telephonyManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        }

    }

    /**
     * Get IMEI of phone.
     *
     * @return IMEI of phone
     * If there is no IMEI on phone return "357374089317919"
     * If TelefonyManager instance == null return empty IMEI
     */

    @SuppressLint("MissingPermission")
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

    /**
     * Getter for TrueTime time and date
     *
     * @return formatted String with TrueTime time and date
     */

    private String getTrueTime() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
        Date trueTimeDate = TrueTimeRx.now();

        return dateFormat.format(trueTimeDate);
    }

    /**
     * Delete message from queue and stop watching for battery charge level
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(reconnect);
        mHandlerBattery.removeCallbacks(askBatteryCharge);
        this.unregisterReceiver(batteryChargeLevelReceiver);

    }

    /**
     * When fires:
     * 1. If TrueTime isn't init, it will init. This may be happen when Application started without
     * internet connection
     * 2. Reconnecting to server happens every ten seconds
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (!TrueTimeRx.isInitialized()) {

            App.initTrueTime();

        }

        mHandler.postDelayed(reconnect, 10000);
    }

    /**
     * Delete message from queue and stop watching for battery charge level
     */
    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(reconnect);
        mHandlerBattery.removeCallbacks(askBatteryCharge);
    }

    /**
     * Receiver for battery charge level
     */

    class BatteryReceiver extends BroadcastReceiver {
        int scale = -1;
        int level = -1;
        int voltage = -1;
        int temp = -1;

        @Override
        public void onReceive(Context context, Intent intent) {

            level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
            voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
            Log.i("BatteryManager", "level is " + level + "/" + scale + ", temp is " + temp + ", voltage is " + voltage);

            batteryLow = level < 15;

            Log.i(TAG, " batteryLow = " + batteryLow);
        }
    }


}