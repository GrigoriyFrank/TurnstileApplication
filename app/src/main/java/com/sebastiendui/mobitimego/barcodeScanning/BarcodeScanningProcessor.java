// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.sebastiendui.mobitimego.barcodeScanning;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.sebastiendui.mobitimego.R;
import com.sebastiendui.mobitimego.screens.MainScreenB;
import com.sebastiendui.mobitimego.screens.RFIDbadgeKeypadD;
import com.sebastiendui.mobitimego.screens.ScanScreen;

import java.io.IOException;
import java.util.List;

/**
 * Barcode Detector.
 */
public class BarcodeScanningProcessor extends VisionProcessorBase<List<FirebaseVisionBarcode>> {

    private static final String TAG = BarcodeScanningProcessor.class.getSimpleName();

    private final FirebaseVisionBarcodeDetector detector;
    private String barcodeValue = null;
    private CameraSource cameraToStop;
    private Activity activity;
    private Context context;
    private boolean isRfidButtonClicked;


    public BarcodeScanningProcessor(CameraSource cameraSource, Activity activity, Context context) {
        // Note that if you know which format of barcode your app is dealing with, detection will be
        // faster to specify the supported barcode formats one by one, e.g.
        // new FirebaseVisionBarcodeDetectorOptions.Builder()
        //     .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
        //     .build();
        this.cameraToStop = cameraSource;
        this.activity = activity;
        this.context = context;
        detector = FirebaseVision.getInstance().getVisionBarcodeDetector();

    }

    /**
     * Stops barcode detector
     */
    @Override
    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Barcode Detector: " + e);
        }
    }

    @Override
    protected Task<List<FirebaseVisionBarcode>> detectInImage(FirebaseVisionImage image) {
        return detector.detectInImage(image);
    }

    /**
     * Getter of barcode value
     */

    public String getBarcodeValue() {
        return barcodeValue;
    }

    /**
     * Read only the first barcode which is added by the detector.
     * <p>
     * Also there are two states:
     * 1. When RFID conversion button clicked
     * 2. When RFID conversion button is not clicked
     * <p>
     * Put values into intent to send them forward.
     *
     * @param barcodes      read barcodes
     * @param frameMetadata frames
     */
    @Override
    protected void onSuccess(
            @NonNull List<FirebaseVisionBarcode> barcodes,
            @NonNull FrameMetadata frameMetadata
    ) {


        for (int i = 0; i < barcodes.size(); ++i) {

            isRfidButtonClicked = ScanScreen.isRfidConversionButtonClicked();
            FirebaseVisionBarcode barcode = barcodes.get(i);
            barcodeValue = barcode.getRawValue();

            shootSound();
            cameraToStop.release();

            Intent startMainScreenB = new Intent(context, MainScreenB.class);
            startMainScreenB.setFlags(
                    Intent.FLAG_ACTIVITY_SINGLE_TOP |
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
            );
            startMainScreenB.putExtra(
                    context.getResources().getString(R.string.direction_json),
                    activity.getIntent().getStringExtra(context.getResources().getString(R.string.direction_json))
            );

            startMainScreenB.putExtra(
                    context.getResources().getString(R.string.data_read_json),
                    barcodeValue);

            startMainScreenB.putExtra(
                    context.getResources().getString(R.string.relay_time_seconds_json),
                    activity.getIntent().getIntExtra(context.getResources().getString(R.string.relay_time_seconds_json), 0)
            );

            startMainScreenB.putExtra(
                    context.getResources().getString(R.string.conf_screens_seconds_json),
                    activity.getIntent().getIntExtra(context.getResources().getString(R.string.conf_screens_seconds_json), 0)
            );

            startMainScreenB.putExtra(
                    context.getResources().getString(R.string.relay_feature_json),
                    activity.getIntent().getBooleanExtra(context.getResources().getString(R.string.relay_feature_json), false)
            );


            int barcodeFormat = barcode.getFormat();

            if (barcodeFormat < 256) {

                startMainScreenB.putExtra(context.getResources().getString(R.string.data_source_json), context.getResources().getString(R.string._1d));
                startMainScreenB.putExtra(context.getResources().getString(R.string.rfidref_json), "");

            } else if (barcodeFormat == 256) {

                startMainScreenB.putExtra(context.getResources().getString(R.string.data_source_json), context.getResources().getString(R.string.qr));
                startMainScreenB.putExtra(context.getResources().getString(R.string.rfidref_json), "");
                // Log.i(TAG, "QR Detected");
            } else {

                startMainScreenB.putExtra(context.getResources().getString(R.string.data_source_json), context.getResources().getString(R.string.unsupported_format));
                startMainScreenB.putExtra(context.getResources().getString(R.string.rfidref_json), "");

            }

            if (isRfidButtonClicked) {

                Intent startRfidKeyboardScreen = new Intent(context, RFIDbadgeKeypadD.class);

                startRfidKeyboardScreen.putExtra(
                        context.getResources().getString(R.string.direction_json),
                        activity.getIntent().getStringExtra(context.getResources().getString(R.string.direction_json))
                );

                startRfidKeyboardScreen.putExtra(
                        context.getResources().getString(R.string.data_source_json),
                        startMainScreenB.getStringExtra(context.getResources().getString(R.string.data_source_json))
                );

                startRfidKeyboardScreen.putExtra(
                        context.getResources().getString(R.string.data_read_json),
                        startMainScreenB.getStringExtra(context.getResources().getString(R.string.data_read_json))
                );

                startRfidKeyboardScreen.putExtra(
                        context.getResources().getString(R.string.relay_time_seconds_json),
                        startMainScreenB.getIntExtra(context.getResources().getString(R.string.relay_time_seconds_json), 0)
                );

                startRfidKeyboardScreen.putExtra(
                        context.getResources().getString(R.string.conf_screens_seconds_json),
                        startMainScreenB.getIntExtra(context.getResources().getString(R.string.conf_screens_seconds_json), 0)
                );

                startRfidKeyboardScreen.putExtra(
                        context.getResources().getString(R.string.relay_feature_json),
                        startMainScreenB.getBooleanExtra(context.getResources().getString(R.string.relay_feature_json), false)
                );

                ScanScreen.setIsRfidConversionButtonClicked(false);
                activity.startActivity(startRfidKeyboardScreen);
                activity.finish();
            } else {
                activity.startActivity(startMainScreenB);

            }

        }
    }

    /**
     * Calls when something wrong with barcode detection
     *
     * @param e exception with details
     */
    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Barcode detection failed " + e);
    }

    /**
     * Fire Camera photo shoot sound when barcode detected
     */
    private void shootSound() {
        AudioManager meng = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int volume = meng.getStreamVolume(AudioManager.STREAM_NOTIFICATION);

        if (volume != 0) {
            MediaPlayer mediaPlayer = null;
            if (mediaPlayer == null)
                mediaPlayer = MediaPlayer.create(context, Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));
            if (mediaPlayer != null)
                mediaPlayer.start();
        }
    }
}
