package fr.rostand.drone;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.secneo.sdk.Helper;

public class DroneApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Helper.install(this);
        MultiDex.install(this);
    }
}
