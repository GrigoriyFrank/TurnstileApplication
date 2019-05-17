package com.sebastiendui.mobitimego.screens;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.sebastiendui.mobitimego.R;

import com.sebastiendui.mobitimego.barcodeScanning.BarcodeScanningProcessor;

import com.sebastiendui.mobitimego.barcodeScanning.CameraSource;
import com.sebastiendui.mobitimego.barcodeScanning.CameraSourcePreview;

import com.sebastiendui.mobitimego.core.BaseActivity;

import com.sebastiendui.mobitimego.nfc.NdefMessageParser;
import com.sebastiendui.mobitimego.nfc.NfcUtils;
import com.sebastiendui.mobitimego.nfc.record.ParsedNdefRecord;

import com.sebastiendui.mobitimego.YoctoAPI.YAPI;
import com.sebastiendui.mobitimego.YoctoAPI.YAPI_Exception;
import com.sebastiendui.mobitimego.YoctoAPI.YRelay;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Class for Scan Screen
 */
public class ScanScreen extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "ScanScreen";
    private static final boolean DEBUG = true;

    private static final String BARCODE_DETECTION = "Barcode Detection";
    private static final long VIBRATE_TIME = 10000000L;

    private static boolean isRfidConversionButtonClicked = false;

    //for camera
    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;

    private YRelay relay;

    private NfcAdapter mAdapter;
    private String tagData = "There is no Data";
    private Intent scanNfcWithRfidKeypadIntent;
    private Intent scanNfcIntent;

    private Button rfidConversionButton;

    private Vibrator vibrator;

    private AlertDialog mDialog;

    private Handler handlerForNfcTagReader = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_screen);

        rfidConversionButton = findViewById(R.id.scan_rfid_conv_button);
        preview = findViewById(R.id.camera_source_preview);

        if (preview == null) {
            Log.d(TAG, "Preview is null");
        }

        createCameraSource(BARCODE_DETECTION);

        cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);

        //buttons
        findViewById(R.id.scan_cancel_button).setOnClickListener(this);
        findViewById(R.id.scan_rfid_conv_button).setOnClickListener(this);

        //Fire Yocto-relay
        try {

            // Pass the application Context to the Yoctopuce Library
            YAPI.EnableUSBHost(getApplicationContext());
            YAPI.RegisterHub("usb");

            relay = YRelay.FirstRelay();

            if (relay != null) {

                // Hot-plug is easy: just check that the device is online
                if (relay.isOnline()) {

                    relay.setState(YRelay.STATE_B);

                    if (relay.getState() == YRelay.STATE_B) {

//                        Toast.makeText(getApplicationContext(), "Relay is ON", Toast.LENGTH_SHORT).show();

                    }

                } else {

//                    Toast.makeText(getApplicationContext(), "Relay is OFF", Toast.LENGTH_SHORT).show();
                }
            } else {

//                Toast.makeText(getApplicationContext(), "Relay is null", Toast.LENGTH_SHORT).show();
            }
        } catch (YAPI_Exception e) {
            Log.e(TAG, e.getLocalizedMessage());

        }//Fire Yocto-relay

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mAdapter == null) {
            showMessage(R.string.error, R.string.no_nfc);
            finish();
            return;
        }

        //vibration
        vibrator = getVibrator();

        if (vibrator != null) {

            vibrator.vibrate(VIBRATE_TIME);
        }

        Log.i(TAG, "In the end of onCreate()");


    }

    /**
     * Create camera source if it doesn't exists. Using only one model of Machine Learning Kit now,
     * but it could be extended by all other models (see samples from GOOGLE).
     *
     * @param model Barcode detection only
     */
    private void createCameraSource(String model) {

        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = new CameraSource(this);
        }


        switch (model) {

            case BARCODE_DETECTION:

                BarcodeScanningProcessor barcodeScanningProcessor = new BarcodeScanningProcessor(this.cameraSource, this, this);
                cameraSource.setMachineLearningFrameProcessor(barcodeScanningProcessor);

                break;

            default:
                Log.e(TAG, "Unknown model: " + model);
                break;
        }

    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                preview.start(cameraSource);

            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    /**
     * There are two states:
     * 1. When RFID conversion button is clicked
     * 2. When RFID conversion button is not clicked.
     * When user click on RFID conversion button this onResume()(see {@Link #onClick}) is called to change Intent
     * to scanNfcWithRfidKeypadIntent in {@Link PendingIntent}. In other case it using scanNfcIntent.
     */
    @Override
    public void onResume() {
        super.onResume();

        //restart camera
        startCameraSource();

        //restart vibration
        if (vibrator != null) {

            vibrator.vibrate(VIBRATE_TIME);
        }

        //turn on the Yocto-relay
        if (relay != null) {

            try {
                relay.setState(YRelay.STATE_B);
            } catch (YAPI_Exception e) {
                e.printStackTrace();
            }
        }

        //ask if NFC adapter is turned on
        if (mAdapter != null) {
            if (!mAdapter.isEnabled()) {
                showWirelessSettingsDialog();
            }

            //this is used for pending to receive scanned results of NFC tag
            PendingIntent mPendingIntentScanNfc;
            NdefMessage mNdefPushMessage;

            if (isRfidConversionButtonClicked) {

                scanNfcWithRfidKeypadIntent = new Intent(this, getClass());
                scanNfcWithRfidKeypadIntent.setFlags(
                        Intent.FLAG_ACTIVITY_SINGLE_TOP |
                                Intent.FLAG_ACTIVITY_CLEAR_TOP
                );
                scanNfcWithRfidKeypadIntent.putExtra(
                        getString(R.string.direction_json),
                        getIntent().getStringExtra(getString(R.string.direction_json))
                );

                scanNfcWithRfidKeypadIntent.putExtra(
                        getString(R.string.relay_time_seconds_json),
                        getIntent().getIntExtra(getString(R.string.relay_time_seconds_json), 0)
                );

                scanNfcWithRfidKeypadIntent.putExtra(
                        getString(R.string.conf_screens_seconds_json),
                        getIntent().getIntExtra(getString(R.string.conf_screens_seconds_json), 0)
                );

                scanNfcWithRfidKeypadIntent.putExtra(
                        getString(R.string.relay_feature_json),
                        getIntent().getBooleanExtra(getString(R.string.relay_feature_json), true)
                );


                mPendingIntentScanNfc = PendingIntent.getActivity(this,
                        1,
                        scanNfcWithRfidKeypadIntent,
                        0);

                mNdefPushMessage = new NdefMessage(new NdefRecord[]{newTextRecord(
                        "Message from NFC Reader :-)", Locale.ENGLISH, true)});


            } else {

                scanNfcIntent = new Intent(this, getClass());
                scanNfcIntent.setFlags(
                        Intent.FLAG_ACTIVITY_SINGLE_TOP |
                                Intent.FLAG_ACTIVITY_CLEAR_TOP
                );
                scanNfcIntent.putExtra(
                        getString(R.string.direction_json),
                        getIntent().getStringExtra(getString(R.string.direction_json))
                );

                scanNfcIntent.putExtra(
                        getString(R.string.relay_time_seconds_json),
                        getIntent().getIntExtra(getString(R.string.relay_time_seconds_json), 0)
                );

                scanNfcIntent.putExtra(
                        getString(R.string.conf_screens_seconds_json),
                        getIntent().getIntExtra(getString(R.string.conf_screens_seconds_json), 0)
                );

                scanNfcIntent.putExtra(
                        getString(R.string.relay_feature_json),
                        getIntent().getBooleanExtra(getString(R.string.relay_feature_json), true)
                );

                mPendingIntentScanNfc = PendingIntent.getActivity(this,
                        1,
                        scanNfcIntent,
                        0);

                mNdefPushMessage = new NdefMessage(new NdefRecord[]{newTextRecord(
                        "Message from NFC Reader :-)", Locale.ENGLISH, true)});

            }

            //this is needed to say for Android that only this Activity can read NFC tags at the moment
            mAdapter.enableForegroundNdefPush(this, mNdefPushMessage);
            mAdapter.enableForegroundDispatch(this, mPendingIntentScanNfc, null, null);


        }
    }

    /**
     * There are two states:
     * 1. When RFID conversion button clicked
     * 2. When RFID conversion button is not clicked.
     * After user scanned the NFC tag this fires after onResume().
     * Then we put data into Intent for each state
     *
     * @param intent intent
     */
    @Override
    public void onNewIntent(Intent intent) {

        if (intent != null) {

            setIntent(intent);

            resolveIntent(intent);


            if (isRfidConversionButtonClicked) {

                Intent launchRFIDkeyboard = new Intent(this, RFIDbadgeKeypadD.class);
                launchRFIDkeyboard.putExtra(getString(R.string.data_read_json), tagData);
                launchRFIDkeyboard.putExtra(getString(R.string.data_source_json), "NFC");
                launchRFIDkeyboard.putExtra(getString(R.string.direction_json), scanNfcWithRfidKeypadIntent.getStringExtra(getString(R.string.direction_json)));
                launchRFIDkeyboard.putExtra(
                        getString(R.string.relay_time_seconds_json),
                        scanNfcWithRfidKeypadIntent.getIntExtra(getString(R.string.relay_time_seconds_json), 0)
                );

                launchRFIDkeyboard.putExtra(
                        getString(R.string.conf_screens_seconds_json),
                        scanNfcWithRfidKeypadIntent.getIntExtra(getString(R.string.conf_screens_seconds_json), 0)
                );

                launchRFIDkeyboard.putExtra(
                        getString(R.string.relay_feature_json),
                        scanNfcWithRfidKeypadIntent.getBooleanExtra(getString(R.string.relay_feature_json), true)
                );
                finish();
                startActivity(launchRFIDkeyboard);

                if (DEBUG) {
                    Log.i(TAG, "onNewIntent(). (isRfidConversionButtonClicked == true), Direction = " + launchRFIDkeyboard.getStringExtra("Direction"));
                    Log.i(TAG, "onNewIntent(). (isRfidConversionButtonClicked == true), DataSource = " + launchRFIDkeyboard.getStringExtra("DataSource"));
                    Log.i(TAG, "onNewIntent(). (isRfidConversionButtonClicked == true), DataRead = " + launchRFIDkeyboard.getStringExtra("DataRead"));
                    Log.i(TAG, "onNewIntent(). (isRfidConversionButtonClicked == true), RelayTimeSeconds = " + launchRFIDkeyboard.getIntExtra("RelayTimeSeconds", 0));
                    Log.i(TAG, "onNewIntent(). (isRfidConversionButtonClicked == true), ConfScreensSeconds = " + launchRFIDkeyboard.getIntExtra("ConfScreensSeconds", 0));
                    Log.i(TAG, "onNewIntent(). (isRfidConversionButtonClicked == true), RelayFeature = " + launchRFIDkeyboard.getBooleanExtra("RelayFeature", true));
                }

            } else {

                Intent launchMainScreenB = new Intent(this, MainScreenB.class);
                launchMainScreenB.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                launchMainScreenB.putExtra(getString(R.string.data_source_json), "NFC");
                launchMainScreenB.putExtra(getString(R.string.data_read_json), tagData);
                launchMainScreenB.putExtra(getString(R.string.rfidref_json), "");
                launchMainScreenB.putExtra(getString(R.string.direction_json), scanNfcIntent.getStringExtra(getString(R.string.direction_json)));
                launchMainScreenB.putExtra(
                        getString(R.string.relay_time_seconds_json),
                        scanNfcIntent.getIntExtra(getString(R.string.relay_time_seconds_json), 0)
                );

                launchMainScreenB.putExtra(
                        getString(R.string.conf_screens_seconds_json),
                        scanNfcIntent.getIntExtra(getString(R.string.conf_screens_seconds_json), 0)
                );

                launchMainScreenB.putExtra(
                        getString(R.string.relay_feature_json),
                        scanNfcIntent.getBooleanExtra(getString(R.string.relay_feature_json), true)
                );
                finish();
                startActivity(launchMainScreenB);

                if (DEBUG) {
                    Log.i(TAG, "onNewIntent(). (isRfidConversionButtonClicked != true), Direction = " + launchMainScreenB.getStringExtra("Direction"));
                    Log.i(TAG, "onNewIntent(). (isRfidConversionButtonClicked != true), DataSource = " + launchMainScreenB.getStringExtra("DataSource"));
                    Log.i(TAG, "onNewIntent(). (isRfidConversionButtonClicked != true), DataRead = " + launchMainScreenB.getStringExtra("DataRead"));
                    Log.i(TAG, "onNewIntent(). (isRfidConversionButtonClicked != true), RFIDRef = " + launchMainScreenB.getStringExtra("RFIDRef"));
                    Log.i(TAG, "onNewIntent(). (isRfidConversionButtonClicked != true), RelayTimeSeconds = " + launchMainScreenB.getIntExtra("RelayTimeSeconds", 0));
                    Log.i(TAG, "onNewIntent(). (isRfidConversionButtonClicked != true), ConfScreensSeconds = " + launchMainScreenB.getIntExtra("ConfScreensSeconds", 0));
                    Log.i(TAG, "onNewIntent(). (isRfidConversionButtonClicked != true), RelayFeature = " + launchMainScreenB.getBooleanExtra("RelayFeature", true));
                }

            }
        }
    }

    /**
     * This method reads NFC {@Link Tag} data and represent it in string
     *
     * @param intent from {@Link onNewIntent()}
     */
    private void resolveIntent(Intent intent) {

        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            } else {
                // Unknown tag type
                byte[] empty = new byte[0];
                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

                byte[] payload = NfcUtils.dumpTagData(tag).getBytes();
                try {
                    TimeUnit.MILLISECONDS.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
                NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
                msgs = new NdefMessage[]{msg};

            }

            tagData = tagDataToString(msgs);

        }
    }

    /**
     * Shows dialog when NFC is turned off. Allow user to go to settings and turn it on
     */
    private void showWirelessSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.nfc_disabled);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.create().show();
        return;
    }

    /**
     * Shows Alert Dialog when there is no NFC adapter on device
     *
     * @param title   Dialog title
     * @param message Dialog message
     */
    private void showMessage(int title, int message) {
        mDialog.setTitle(title);
        mDialog.setMessage(getText(message));
        mDialog.show();
    }

    /**
     * Stops the camera.
     * Disable this Activity be the only one for reading NFC on device.
     * Turn off vibrator.
     * Turn off Yocto-relay
     */
    @Override
    protected void onPause() {

        super.onPause();
        preview.stop();
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
            mAdapter.disableForegroundNdefPush(this);
        }
        if (vibrator != null) {

            vibrator.cancel();
        }

        if (relay != null) {

            try {
                relay.setState(YRelay.STATE_A);
            } catch (YAPI_Exception e) {
                e.printStackTrace();
            }
        }
        rfidConversionButton.setEnabled(true);

    }

    /**
     * Destroy Camera.
     * Turn off Yocto-relay.
     * Turn off vibrator.
     * Change state of RFID conversion button to false
     */

    @Override
    public void onDestroy() {

        super.onDestroy();

        if (cameraSource != null) {
            cameraSource.release();
        }

        if (relay != null) {

            try {
                relay.setState(YRelay.STATE_A);
            } catch (YAPI_Exception e) {
                e.printStackTrace();
            }
        }

        if (vibrator != null) {

            vibrator.cancel();
        }

        setIsRfidConversionButtonClicked(false);
    }

    /**
     * In case when RFID conversion button clicked we call onResume().
     * This is needed to change Intent that we will be put into PendingIntent
     *
     * @param v button
     */
    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (id == R.id.scan_rfid_conv_button) {
            //change state
            isRfidConversionButtonClicked = true;
            rfidConversionButton.setEnabled(false);

            // it is needed for change Intent in PendingIntent
            onResume();

        }

        if (id == R.id.scan_cancel_button) {

            Intent intent = new Intent(this, MainScreenB.class);

            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            startActivity(intent);
        }


    }

    /**
     * Getter for isRfidConversionButtonClicked
     *
     * @return isRfidConversionButtonClicked state of RFID conversion button (clicked or not)
     */
    public static boolean isRfidConversionButtonClicked() {
        return isRfidConversionButtonClicked;
    }

    /**
     * Setter for isRfidConversionButtonClicked
     *
     * @param isRfidConversionButtonClicked state of RFID conversion button (clicked or not)
     */

    public static void setIsRfidConversionButtonClicked(boolean isRfidConversionButtonClicked) {
        ScanScreen.isRfidConversionButtonClicked = isRfidConversionButtonClicked;
    }

    /**
     * Create new NdefRecord for NdefMessage
     *
     * @param text
     * @param locale
     * @param encodeInUtf8
     * @return
     */
    private NdefRecord newTextRecord(String text, Locale locale, boolean encodeInUtf8) {
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));

        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
        byte[] textBytes = text.getBytes(utfEncoding);

        int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        char status = (char) (utfBit + langBytes.length);

        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);

        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
    }

    /**
     * Convert array of NdefMessage to string
     *
     * @param msgs what to convert to string
     * @return converted string
     */
    String tagDataToString(NdefMessage[] msgs) {

        StringBuilder temp = new StringBuilder();
        if (msgs == null || msgs.length == 0) {
            return null;
        }

        List<ParsedNdefRecord> records = NdefMessageParser.parse(msgs[0]);
        final int size = records.size();
        for (int i = 0; i < size; i++) {

            ParsedNdefRecord record = records.get(i);
            temp = temp.append(record.getText());
        }

        return temp.toString();
    }


}
