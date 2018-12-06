/*
 * Pricer Server Software
 *
 * Confidential Property of Pricer AB (publ). Copyright © 1998-2017 Pricer AB (publ),
 * Box 215,Västra Järnvägsgatan 7, SE-101 24 Stockholm, Sweden. All rights reserved.
 */
package se.pricer.example.widget.android.polestar;

import android.content.Context;
import android.location.Location;

import com.polestar.naosdk.api.external.NAOERRORCODE;
import com.polestar.naosdk.api.external.NAOLocationHandle;
import com.polestar.naosdk.api.external.NAOLocationListener;
import com.polestar.naosdk.api.external.NAOSensorsListener;
import com.polestar.naosdk.api.external.NAOSyncListener;
import com.polestar.naosdk.api.external.TNAOFIXSTATUS;

import se.pricer.example.widget.android.R;
import se.pricer.widget.android.PricerMapWidget;
import se.pricer.widget.android.PricerPosition;

public class LocationService implements NAOLocationListener, NAOSensorsListener, NAOSyncListener {

    private NAOLocationHandle handle;
    private PricerMapWidget map;
    private final static String TRANSFORMATION = "POLESTAR";

    public LocationService(Context context, PricerMapWidget map) {
        this.map = map;
        this.handle = new NAOLocationHandle(context, NaoService.class,
                context.getResources().getString(R.string.polestarApiKey), this, this);
        handle.synchronizeData(this);
    }

    public void start() {
        handle.start();
    }

    public void stop() {
        handle.stop();
    }

    @Override
    public void onLocationChanged(Location location) {
        PricerPosition position = new PricerPosition(
                (float) location.getLatitude(),
                (float) location.getLongitude(),
                0);
        map.setUserPosition(position, TRANSFORMATION);
    }

    @Override
    public void onLocationStatusChanged(TNAOFIXSTATUS tnaofixstatus) {

    }

    @Override
    public void onEnterSite(String s) {

    }

    @Override
    public void onExitSite(String s) {

    }

    @Override
    public void onError(NAOERRORCODE naoerrorcode, String s) {

    }

    @Override
    public void requiresCompassCalibration() {

    }

    @Override
    public void requiresWifiOn() {

    }

    @Override
    public void requiresBLEOn() {

    }

    @Override
    public void requiresLocationOn() {

    }

    @Override
    public void onSynchronizationSuccess() {

    }

    @Override
    public void onSynchronizationFailure(NAOERRORCODE naoerrorcode, String s) {

    }
}
