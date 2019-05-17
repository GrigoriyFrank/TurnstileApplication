package com.sebastiendui.mobitimego.screens;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.sebastiendui.mobitimego.R;
import com.sebastiendui.mobitimego.barcodeScanning.CameraSource;
import com.sebastiendui.mobitimego.core.BaseActivity;

import java.io.IOException;

import static android.support.constraint.Constraints.TAG;

/**
 * Class for Confirmation Screen:
 * 1. Confirmation Welcome Screen
 * 2. Confirmation Good Bye Screen
 * 3. KO Screen
 * Screen representation depends on parameters from GET /registerTeam output:
 * "InssValid", "INSS"
 * Also it depends on internet connection, when lost - text with "No Internet Connection" appears
 */
public class ConfirmationScreen extends BaseActivity implements View.OnClickListener {

    private ImageView okImageView;
    private ImageView notOkImageView;

    private TextView welcomeTextView;
    private TextView outOfHours;
    private TextView nameTextView;
    private TextView company1TextView;
    private TextView inssOkTextView;

    private Intent intentFromMainScreenB;

    private CameraSource cameraSource = null;

    /**
     * Used for turn on and turn off the Camera torch
     */

    private Handler mHandlerForTorch = new Handler();

    /**
     * Used for closing this Screen and return to Main Screen
     */
    private Handler mHandlerForReturnToMainScreen = new Handler();

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "onCreate(). ConfirmationScreen");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirmation_screen);

        okImageView = findViewById(R.id.ok_image_view);
        notOkImageView = findViewById(R.id.not_ok_image_view);

        welcomeTextView = findViewById(R.id.goodbye_welcome);
        outOfHours = findViewById(R.id.out_of_hours);
        nameTextView = findViewById(R.id.name);
        company1TextView = findViewById(R.id.company_name);
        inssOkTextView = findViewById(R.id.inss_ok);

        intentFromMainScreenB = getIntent();

        if (intentFromMainScreenB == null) {

            this.finish();
        }

        boolean inssValid = intentFromMainScreenB.getBooleanExtra(getString(R.string.inss_valid_json), false);

        if (cameraSource == null) {
            cameraSource = new CameraSource(this);
        }

        try {
            showViews(inssValid);
        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    /**
     * Return to Main Screen
     */
    private Runnable returnToMainScreen = new Runnable() {
        public void run() {

            finish();
            Log.i(TAG, " return to main screen... ");
        }
    };

    /**
     * Turn on the Camera torch
     */
    private Runnable turnOnTorch = new Runnable() {
        public void run() {

            try {
                cameraSource.startForConfirmationScreen();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i(TAG, " turnOnTorch... ");
        }
    };

    /**
     * Turn off the Camera torch
     */
    private Runnable turnOffTorch = new Runnable() {
        public void run() {

            if (cameraSource != null) {
                cameraSource.release();
            }
            Log.i(TAG, " turnOffTorch... ");
        }
    };


    /**
     * Refreshing this Screen
     *
     * @param isOk value of "InssValid" from GET /registerTeam output
     */
    private void showViews(boolean isOk) {

        String direction = intentFromMainScreenB.getStringExtra(getString(R.string.direction_json));
        Log.i("ConfirmationScreen", "" + intentFromMainScreenB.getStringExtra(getString(R.string.direction_json)));

        if (direction.equals("OUT")) {
            welcomeTextView.setText(R.string.good_bye);
        } else {
            welcomeTextView.setText(R.string.welcome);
        }


        if (!isOk) {

            notOkImageView.setVisibility(View.VISIBLE);
            okImageView.setVisibility(View.GONE);
            outOfHours.setVisibility(View.GONE);
            nameTextView.setText(intentFromMainScreenB.getStringExtra(getString(R.string.name_json)));
            company1TextView.setText(intentFromMainScreenB.getStringExtra(getString(R.string.company_json)));
            okImageView.setVisibility(View.GONE);
            inssOkTextView.setText(intentFromMainScreenB.getStringExtra(getString(R.string.inss_json)));

            mHandlerForReturnToMainScreen.postDelayed(
                    returnToMainScreen,
                    (long) intentFromMainScreenB.getIntExtra(getString(R.string.conf_screens_seconds_json), 5) * 1000
            );

        } else {

            nameTextView.setText(intentFromMainScreenB.getStringExtra(getString(R.string.name_json)));
            company1TextView.setText(intentFromMainScreenB.getStringExtra(getString(R.string.company_json)));
            inssOkTextView.setText(intentFromMainScreenB.getStringExtra(getString(R.string.inss_json)));

            if (intentFromMainScreenB.getBooleanExtra(getString(R.string.relay_feature_json), true)) {

                //need to be delayed, because the turn On of torch happens earlier then screen appears
                mHandlerForTorch.postDelayed(turnOnTorch, 250);

                mHandlerForTorch.postDelayed(turnOffTorch, (long) intentFromMainScreenB.getIntExtra(getString(R.string.relay_time_seconds_json), 5) * 1000);
                mHandlerForReturnToMainScreen.postDelayed(returnToMainScreen, (long) intentFromMainScreenB.getIntExtra(getString(R.string.conf_screens_seconds_json), 5) * 1000);


            } else {

                mHandlerForReturnToMainScreen.postDelayed(
                        returnToMainScreen,
                        (long) intentFromMainScreenB.getIntExtra(getString(R.string.conf_screens_seconds_json), 5) * 1000
                );
            }


        }

    }

    /**
     * Closes screen when user taps
     *
     * @param v all screen area
     */
    @Override
    public void onClick(View v) {

        this.finish();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(TAG, " onNewIntent(). Confirmation Screen");
    }

    /**
     * Destroy the Camera. Remove all messages from queue
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (cameraSource != null) {
            cameraSource.release();
        }

        mHandlerForTorch.removeCallbacks(turnOnTorch);
        mHandlerForReturnToMainScreen.removeCallbacks(returnToMainScreen);
    }


}
