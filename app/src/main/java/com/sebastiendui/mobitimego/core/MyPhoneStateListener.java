package com.sebastiendui.mobitimego.core;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.telephony.PhoneStateListener;

import android.telephony.SignalStrength;
import android.widget.ImageView;

import com.sebastiendui.mobitimego.R;

/**
 * This class is for listening of network signal strength changing
 */
public class MyPhoneStateListener extends PhoneStateListener {

    private final static String TAG = "MyPhoneStateListener";
    private ImageView signalImageView;

    public MyPhoneStateListener(Context context, ImageView signalImageView) {
        Context mContext = context;
        this.signalImageView = signalImageView;

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);
        int signal = signalStrength.getLevel();

        //changes the image
        switch (signal) {

            case (0):
                signalImageView.setBackgroundResource(R.drawable.bars0);
                break;

            case (1):
                signalImageView.setBackgroundResource(R.drawable.bar1);
                break;

            case (2):
                signalImageView.setBackgroundResource(R.drawable.bars2);
                break;

            case (3):
                signalImageView.setBackgroundResource(R.drawable.bars3);
                break;

            case (4):
                signalImageView.setBackgroundResource(R.drawable.bars4);
                break;
            case (5):
                signalImageView.setBackgroundResource(R.drawable.bars5);
                break;

            default:
                signalImageView.setBackgroundResource(R.drawable.bars5);
                break;
        }
    }


}
