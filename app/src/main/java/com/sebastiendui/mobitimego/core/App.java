package com.sebastiendui.mobitimego.core;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.instacart.library.truetime.TrueTimeRx;
import com.sebastiendui.mobitimego.service.RestartService;

import io.fabric.sdk.android.Fabric;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Class for Application.
 * To avoid creating multiple instances of Retrofit and TrueTime
 * Fire service for restarting
 */
public class App extends Application {

    private static final String TAG = "App";
    private static MobiTimeGoApi mobiTimeGoApi;


    @Override
    public void onCreate() {
        super.onCreate();

       // startService(new Intent(this, RestartService.class));

        Fabric.with(this, new Crashlytics());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://socket.mobitime.net:3002")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mobiTimeGoApi = retrofit.create(MobiTimeGoApi.class);

        initTrueTime();

    }

    /**
     * Getter for {@Link MobiTimeGoApi}
     * @return the instance of MobiTimeGoApi interface
     */
    public static MobiTimeGoApi getApi() {
        return mobiTimeGoApi;
    }

    /**
     * Initialize TrueTime time and date
     */
    @SuppressLint("CheckResult")
    public static void initTrueTime() {

        // This is for receiving true time and date
        TrueTimeRx.build()
                .initializeRx("time.google.com")
                .subscribeOn(Schedulers.io())
                .subscribe(date -> {
                    Log.i(TAG, "TrueTime was initialized and we have a time: " + date);
                }, Throwable::printStackTrace);

    }
}
