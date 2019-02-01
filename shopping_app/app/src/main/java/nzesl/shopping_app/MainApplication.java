/*
 * Pricer Server Software
 *
 * Confidential Property of Pricer AB (publ). Copyright © 1998-2017 Pricer AB (publ),
 * Box 215,Västra Järnvägsgatan 7, SE-101 24 Stockholm, Sweden. All rights reserved.
 */
package nzesl.shopping_app;

import android.support.multidex.MultiDexApplication;

import se.pricer.widget.android.PricerApi;

public class MainApplication extends MultiDexApplication {

  @Override
  public void onCreate() {
    // Start the PricerApi, edit settings.xml and enter your cloud api settings
    PricerApi.getInstance().startApi(
        getResources().getString(R.string.storeUuid),
        getResources().getString(R.string.apiUsername),
        getResources().getString(R.string.apiKey));

    super.onCreate();
  }
}
