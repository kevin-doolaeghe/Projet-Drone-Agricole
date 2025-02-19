package fr.rostand.drone.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.products.Aircraft;
import dji.sdk.products.HandHeld;
import dji.sdk.sdkmanager.DJISDKManager;
import fr.rostand.drone.R;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    public static final String SHARED_PREFERENCES_ID = "drone";

    public static final String FLAG_CONNECTION_CHANGE = "dji_sdk_connection_change";
    private Handler mHandler;

    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
    };
    private List<String> missingPermission = new ArrayList<>();
    private AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    private static final int REQUEST_PERMISSION_CODE = 12345;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        // When the compile and target version is higher than 22, please request the following permission at runtime to ensure the SDK works well.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            checkAndRequestPermissions();
        }

        //Initialize DJI SDK Manager
        mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Checks if there is any missing permissions, and
     * requests runtime permission if needed.
     */
    private void checkAndRequestPermissions() {
        // Check for permissions
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }

        // Request for missing permissions
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showToast(getString(R.string.msg_permission_grant_required));
            ActivityCompat.requestPermissions(this, missingPermission.toArray(new String[missingPermission.size()]), REQUEST_PERMISSION_CODE);
        }
    }

    /**
     * Result of runtime permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check for granted permission and remove from missing list
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i--) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermission.remove(permissions[i]);
                }
            }
        }

        // If there is enough permission, we will start the registration
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else {
            showToast(getString(R.string.error_missing_permissions));
        }
    }

    private void startSDKRegistration() {
        if (isRegistrationInProgress.compareAndSet(false, true)) {
            AsyncTask.execute(() -> {
                showToast(getString(R.string.msg_sdk_registering));
                DJISDKManager.getInstance().registerApp(MainActivity.this.getApplicationContext(), new DJISDKManager.SDKManagerCallback() {
                    @Override
                    public void onRegister(DJIError djiError) {
                        if (djiError == DJISDKError.REGISTRATION_SUCCESS) {
                            showToast(getString(R.string.msg_sdk_register_success));
                            DJISDKManager.getInstance().startConnectionToProduct();

                            Intent intent = new Intent(MainActivity.this, FlightPlanListActivity.class);
                            startActivity(intent);

                            finish();
                        } else {
                            showToast(getString(R.string.error_sdk_register_failure));
                        }
                        Log.v(TAG, djiError.getDescription());
                    }

                    @Override
                    public void onProductDisconnect() {
                        Log.d(TAG, "onProductDisconnect");
                        showToast(getString(R.string.msg_product_disconnected));
                        notifyStatusChange();
                    }

                    @Override
                    public void onProductConnect(BaseProduct baseProduct) {
                        Log.d(TAG, String.format("onProductConnect newProduct : %s", baseProduct));
                        showToast(getString(R.string.msg_product_connected));
                        notifyStatusChange();
                    }

                    @Override
                    public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent oldComponent, BaseComponent newComponent) {
                        if (newComponent != null) {
                            newComponent.setComponentListener((boolean isConnected) -> {
                                Log.d(TAG, "onComponentConnectivityChanged : " + isConnected);
                                notifyStatusChange();
                            });
                        }
                        Log.d(TAG, String.format("onComponentChange key : %s, oldComponent : %s, newComponent : %s", componentKey, oldComponent, newComponent));
                    }
                });
            });
        }
    }

    private void notifyStatusChange() {
        mHandler.removeCallbacks(updateRunnable);
        mHandler.postDelayed(updateRunnable, 500);
    }

    private Runnable updateRunnable = () -> {
        Intent intent = new Intent(FLAG_CONNECTION_CHANGE);
        sendBroadcast(intent);
    };

    private void showToast(final String toastMsg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show());
    }
}
