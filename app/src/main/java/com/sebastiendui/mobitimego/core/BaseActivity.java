package com.sebastiendui.mobitimego.core;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.sebastiendui.mobitimego.screens.StartUpScreenA;

/**
 * The main function of this class is to prevent app crashing or calls to NFC reading/writing apps, when
 * user try to read NFC tags outside of Scan Screen
 * <p>
 * Also there are functions:
 *  - to prevent hide app after Home Button pressed
 *  - restart app after crash
 */

public class BaseActivity extends Activity {

    private static final String TAG = BaseActivity.class.getSimpleName();
    private static final int NOTIFY_ID = 1;

    private NfcAdapter mAdapter;
    private PendingIntent pendingIntent;

    private HomeWatcher mHomeWatcher;

    private Handler mHandler = new Handler();

    private Vibrator vibrator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.i("BaseActivity", "onCreate(), Base Activity");

        //createAndStartHomeWatcher();

        //restartAppAfterCrash();

        //vibration
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

    }

    /**
     * Used for creating and start listener for Home button event
     *
     */

    private void createAndStartHomeWatcher() {

        mHomeWatcher = new HomeWatcher(this);
        mHomeWatcher.setOnHomePressedListener(new OnHomePressedListener() {
            @Override
            public void onHomePressed() {

                mHandler.removeCallbacks(backActivityInForeground);
                mHandler.postDelayed(backActivityInForeground, 10000);

                Log.i("HomeButton", " Home button press detected");
            }

            @Override
            public void onHomeLongPressed() {
            }
        });
        mHomeWatcher.startWatch();
    }

    /**
     * Return Activity in foreground
     */

    private Runnable backActivityInForeground = new Runnable() {
        public void run() {
            Intent backActivityInForeground = new Intent(getApplicationContext(), StartUpScreenA.class);
            backActivityInForeground.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(backActivityInForeground);
            Log.i(TAG, " backActivityInForeground... ");
        }
    };

    /**
     * Restart app after crash. It fires when uncaught exception occur
     */

    private void restartAppAfterCrash() {

        Thread.UncaughtExceptionHandler onRuntimeError = new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread thread, Throwable ex) {
                //Try starting the Activity again

                Log.i(TAG, "uncaught exception");

                ex.printStackTrace();
                System.exit(0);


            }
        };

        Thread.setDefaultUncaughtExceptionHandler(onRuntimeError);
    }

    protected void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
        }
        Log.i(TAG, " onPause()");

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, " onRestart()");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, " onStart()");
    }

    protected void onResume() {
        super.onResume();
        if (mAdapter != null) {
            mAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
        Log.i(TAG, " onResume()");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.i(TAG, "Back button pressed!");
        }
        return super.onKeyDown(keyCode, event);

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, " onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, " onDestroy()");

       // mHomeWatcher.stopWatch();

        if (vibrator != null) {

            vibrator.cancel();
        }
    }

    /**
     * Getter for {@link Vibrator} instance
     * @return Vibrator instance
     */

    public Vibrator getVibrator() {
        return vibrator;
    }

}
