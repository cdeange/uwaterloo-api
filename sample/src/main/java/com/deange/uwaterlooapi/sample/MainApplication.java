package com.deange.uwaterlooapi.sample;

import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.deange.uwaterlooapi.sample.controller.EncryptionController;
import com.deange.uwaterlooapi.sample.controller.WatcardManager;
import com.deange.uwaterlooapi.sample.utils.FontUtils;
import com.deange.uwaterlooapi.sample.utils.NetworkController;
import com.deange.uwaterlooapi.sample.utils.Px;

import net.danlew.android.joda.JodaTimeAndroid;

import pl.tajchert.nammu.Nammu;

public class MainApplication
    extends MultiDexApplication {

  private static final String TAG = MainApplication.class.getSimpleName();

  @Override
  public void onCreate() {
    super.onCreate();

    Log.v(TAG, "onCreate()");

    // Initialize the crash reporting wrapper and analytics reporting wrapper
    CrashReporting.init(this);
    Analytics.init(this);

    // Set up connectivity manager
    NetworkController.init(this);

    // Permissions library
    Nammu.init(this);

    // Set up dp-px converter
    Px.init(this);

    // Set up Calligraphy library
    FontUtils.init(this);

    // Joda Time config
    JodaTimeAndroid.init(this);

    // KeyStore loader
    EncryptionController.init(this);

    // Watcard manager
    WatcardManager.init(this);
  }
}
