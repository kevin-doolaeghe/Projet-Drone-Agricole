package fr.rostand.drone.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import dji.common.error.DJIError;
import dji.common.flightcontroller.ConnectionFailSafeBehavior;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.FlightOrientationMode;
import dji.common.flightcontroller.GPSSignalLevel;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointAction;
import dji.common.mission.waypoint.WaypointActionType;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionDownloadEvent;
import dji.common.mission.waypoint.WaypointMissionExecutionEvent;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.common.mission.waypoint.WaypointMissionUploadEvent;
import dji.common.model.LocationCoordinate2D;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import dji.sdk.products.Aircraft;
import fr.rostand.drone.R;
import fr.rostand.drone.view.MainActivity;

public class DroneFlightController {
    private static final String TAG = "DroneFlightController";

    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private static DroneFlightController instance; // Singleton instance

    // Drone variables
    private FlightController mFlightController;

    private GPSSignalLevel mGPSSignalLevel;
    private double mCurrentLatitude = 181;
    private double mCurrentLongitude = 181;

    private MutableLiveData<List<Waypoint>> mWaypointList = new MutableLiveData<>();
    private float mAltitude;

    private MutableLiveData<String> mLocation = new MutableLiveData<>();
    private MutableLiveData<String> mMission = new MutableLiveData<>();
    private MutableLiveData<String> mStatus = new MutableLiveData<>();
    private MutableLiveData<Integer> mProgress = new MutableLiveData<>();

    /**
     * Constructor
     */
    private DroneFlightController(Context context) {
        mContext = context;
        mSharedPreferences = context.getSharedPreferences(MainActivity.SHARED_PREFERENCES_ID, 0);
    }

    /**
     * Singleton pattern : only one instance
     */
    public static DroneFlightController getInstance(Context context) {
        if (instance == null) {
            instance = new DroneFlightController(context);
        }
        return instance;
    }

    /**
     * Getters and setters
     */
    public LiveData<List<Waypoint>> getWaypointList() {
        return mWaypointList;
    }

    public void setWaypointList(List<Waypoint> waypointList) {
        mWaypointList.postValue(waypointList);
    }

    public LiveData<String> getMission() {
        return mMission;
    }

    public void setMission(String mission) {
        mMission.postValue(mission);
    }

    public LiveData<String> getStatus() {
        return mStatus;
    }

    public void setStatus(String status) {
        mStatus.postValue(status);
    }

    public LiveData<String> getLocation() {
        return mLocation;
    }

    public void setLocation(String location) {
        mLocation.postValue(location);
    }

    public LiveData<Integer> getProgress() {
        return mProgress;
    }

    public void setProgress(int progress) {
        mProgress.postValue(progress);
    }

    /**
     * Get aircaft instance and init variables
     */
    public void initComponents() {
        Aircraft aircraft = Drone.getAircraftInstance();

        if (aircraft != null && aircraft.isConnected()) {
            mFlightController = aircraft.getFlightController();
        }

        if (mFlightController != null) {
            mFlightController.setConnectionFailSafeBehavior(ConnectionFailSafeBehavior.GO_HOME, null);
            mFlightController.setFlightOrientationMode(FlightOrientationMode.AIRCRAFT_HEADING, null);
            mFlightController.setStateCallback((@NonNull FlightControllerState flightControllerState) -> {
                mCurrentLatitude = flightControllerState.getAircraftLocation().getLatitude();
                mCurrentLongitude = flightControllerState.getAircraftLocation().getLongitude();
                mGPSSignalLevel = flightControllerState.getGPSSignalLevel();
            });
            updateDroneLocation();
        }
    }

    /**
     * Set current drone location
     */
    private void updateDroneLocation() {
        setLocation(R.string.title_gps_signal + " " + mGPSSignalLevel + "\n" + R.string.title_latitude + " " + mCurrentLatitude + "\n" + R.string.title_longitude + " " + mCurrentLongitude);
    }

    /**
     * Check if GPS coordination matches
     */
    private boolean checkGpsCoordination(double latitude, double longitude) {
        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f && longitude != 0f);
    }

    /**
     * Get drone instance
     */
    private WaypointMissionOperator getWaypointMissionOperator() {
        return Drone.getWaypointMissionOperator();
    }

    /**
     * Display message on screen
     */
    private void showToast(final String toastMsg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(mContext, toastMsg, Toast.LENGTH_SHORT).show());
    }

    /**
     * Calculate the waypoint list
     * Return if the list is successfully created
     */
    public void generateWaypointList(double lat1, double lon1, double lat2, double lon2) {
        List<Waypoint> waypointList = new ArrayList<>();

        setMission("");
        setStatus("");

        mWaypointList.postValue(new ArrayList<>());

        // If coordinates are wrong : can't generate waypoint list
        if (!checkGpsCoordination(lat1, lon1) || !checkGpsCoordination(lat2, lon2)) {
            setMission("No mission created !");
            setStatus("Wrong coordinates !");
            return;
        }

        // Prepare variables
        if (lat1 > lat2) {
            double temp = lat1;
            lat1 = lat2;
            lat2 = temp;
        }

        if (lon1 > lon2) {
            double temp = lon1;
            lon1 = lon2;
            lon2 = temp;
        }

        double latGapLength = (lat2 - lat1) * 111319;
        double lonGapLength = (lon2 - lon1) * 111319;

        final float minAltitude = mSharedPreferences.getFloat("minAltitude", 10);
        final float maxAltitude = mSharedPreferences.getFloat("maxAltitude", 15);
        mAltitude = minAltitude;

        final double ratio = 1.78;

        final int nbImgMax = 99;
        int latAxisImgCount;
        int lonAxisImgCount;
        int totalImgCount;
        double imgHeight;
        double imgWidth;

        boolean running = true;

        // Loop to generate waypoint list
        while (running) {
            imgHeight = mAltitude * 7.7 * 5.6 / 20 / Math.sqrt(1 + ratio * ratio);
            imgWidth = imgHeight * ratio;
            Log.d(TAG, "Image :\nHeight : " + imgHeight + "m | Width : " + imgWidth + "m");

            latAxisImgCount = (int) Math.ceil(latGapLength / imgHeight); // Aircraft is facing north
            lonAxisImgCount = (int) Math.ceil(lonGapLength / imgWidth);
            totalImgCount = latAxisImgCount * lonAxisImgCount;

            // If can create list
            if (totalImgCount <= nbImgMax) {
                double latToAdd = imgHeight  / 111319;
                double lonToAdd = imgWidth / 111319;

                for (int i = 0; i < lonAxisImgCount; i++) {
                    for (int j = 0; j < latAxisImgCount; j++) {
                        double latitude;
                        double longitude;

                        if (i % 2 == 0) {
                            latitude = lat1 + (j + 0.5) * latToAdd;
                        } else {
                            latitude = lat1 + (latAxisImgCount - j - 0.5) * latToAdd;
                        }
                        longitude = lon1 + (i + 0.5) * lonToAdd;

                        waypointList.add(new Waypoint(latitude, longitude, mAltitude));
                    }
                }

                setWaypointList(waypointList);
                setMission("Calcul du parcours terminé avec succès !!" + "\nAltitude de vol : " + mAltitude + "m" + "\nImages nécessaires : "
                        + totalImgCount + " (" + latAxisImgCount + "x" + lonAxisImgCount + ")");
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putInt("latAxisImgCount", latAxisImgCount);
                editor.putInt("lonAxisImgCount", lonAxisImgCount);
                editor.apply();
                running = false;
            } else {
                // If can't create mission
                if (mAltitude == maxAltitude) {
                    setMission("No mission created !");
                    setStatus("Zone à cartographier trop grande !");
                    running = false;
                } else {
                    mAltitude++;
                }
            }
        }
    }

    /**
     * Create the mission with the calculated coordinates
     */
    public boolean createMission() {
        // If waypoint list is empty : mission isn't created
        if (mWaypointList.getValue().isEmpty()) {
            return false;
        }

        if (mFlightController != null) {
            // Get current drone position
            mCurrentLatitude = mFlightController.getState().getAircraftLocation().getLatitude();
            mCurrentLongitude = mFlightController.getState().getAircraftLocation().getLongitude();

            if (Double.isNaN(mCurrentLatitude) || Double.isNaN(mCurrentLongitude) && !checkGpsCoordination(mCurrentLatitude, mCurrentLongitude)) {
                setStatus("Le GPS n'est pas prêt !");
                return false;
            }

            mFlightController.setHomeLocation(new LocationCoordinate2D(mCurrentLatitude, mCurrentLongitude), null);

            WaypointMissionFinishedAction finishedAction = WaypointMissionFinishedAction.AUTO_LAND;
            WaypointMissionHeadingMode headingMode = WaypointMissionHeadingMode.TOWARD_POINT_OF_INTEREST;

            WaypointMission.Builder waypointMissionBuilder;
            waypointMissionBuilder = new WaypointMission.Builder()
                    .finishedAction(finishedAction)
                    .headingMode(headingMode)
                    .setPointOfInterest(new LocationCoordinate2D(85, 0))
                    .autoFlightSpeed(2)
                    .maxFlightSpeed(2)
                    .flightPathMode(WaypointMissionFlightPathMode.NORMAL);

            // Re-create the mission
            Waypoint homePoint = new Waypoint(mCurrentLatitude, mCurrentLongitude, mAltitude);
            List<Waypoint> waypointList = new ArrayList<>();
            waypointList.add(homePoint);
            for (Waypoint waypoint : mWaypointList.getValue()) {
                waypointList.add(waypoint);
            }
            waypointList.add(homePoint);

            // Prepare to upload mission
            waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
            waypointMissionBuilder.setExitMissionOnRCSignalLostEnabled(true);

            if (waypointMissionBuilder.getWaypointList().size() > 0) {
                for (int i = 0; i < waypointMissionBuilder.getWaypointList().size(); i++) {
                    waypointMissionBuilder.getWaypointList().get(i).altitude = mAltitude;
                    if(i != 0 && i != waypointMissionBuilder.getWaypointList().size() - 1) {
                        // Set waypoint actions
                        waypointMissionBuilder.getWaypointList().get(i).addAction(new WaypointAction(WaypointActionType.GIMBAL_PITCH, -90)); // Gimbal's pitch at -90°
                        waypointMissionBuilder.getWaypointList().get(i).addAction(new WaypointAction(WaypointActionType.STAY, 500)); // Wait 500ms
                        waypointMissionBuilder.getWaypointList().get(i).addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 0)); // Take a photo
                    }
                }
            }

            // Prepare WaypointMissionOperator
            getWaypointMissionOperator().cancelUploadingMission(null);
            getWaypointMissionOperator().clearMission();
            getWaypointMissionOperator().addListener(new WaypointMissionOperatorListener() {
                @Override
                public void onDownloadUpdate(@NonNull WaypointMissionDownloadEvent waypointMissionDownloadEvent) {

                }

                @Override
                public void onUploadUpdate(@NonNull WaypointMissionUploadEvent waypointMissionUploadEvent) {

                }

                @Override
                public void onExecutionUpdate(@NonNull WaypointMissionExecutionEvent waypointMissionExecutionEvent) {
                    // Calculate mission advancement
                    int percent = (int) ((((double) waypointMissionExecutionEvent.getProgress().targetWaypointIndex) / ((double) waypointMissionExecutionEvent.getProgress().totalWaypointCount)) * 100);
                    showToast("Mission completed at : " + percent + "%");
                    setProgress(percent);
                }

                @Override
                public void onExecutionStart() {
                    setProgress(0);
                }

                @Override
                public void onExecutionFinish(@Nullable DJIError djiError) {
                    showToast("Finish :)");
                    setProgress(0);
                }
            });

            // Load mission
            DJIError error = getWaypointMissionOperator().loadMission(waypointMissionBuilder.build());
            if (error != null) {
                showToast("Waypoint load failed !" + error.getDescription());
                setStatus("Error while loading..");
                waypointList.clear();
                return false;
            }

            showToast("Waypoint load succeeded :)");
            setStatus(getWaypointMissionOperator().getLoadedMission().getWaypointCount() + " waypoints loaded");
            return true;
        } else {
            setStatus("Drone not connected");
            return false;
        }
    }

    /**
     * Upload mission
     */
    public void uploadMission() {
        if (mFlightController != null) {
            updateDroneLocation();
            getWaypointMissionOperator().uploadMission((DJIError djiError) -> {
                if (djiError == null) {
                    showToast("Mission upload successfully !");
                } else {
                    showToast("Mission upload failed, error : " + djiError.getDescription() + ". Retrying...");
                    getWaypointMissionOperator().retryUploadMission(null);
                }
            });
        }
    }

    /**
     * Start mission
     */
    public void startMission() {
        if (mFlightController != null) {
            getWaypointMissionOperator().startMission((DJIError djiError) -> {
                showToast("Mission start : " + (djiError == null ? "Successfully" : djiError.getDescription()));
            });
        }
    }

    /**
     * Stop mission
     */
    public void stopMission() {
        if (mFlightController != null) {
            getWaypointMissionOperator().stopMission((DJIError djiError) -> {
                showToast("Mission stop : " + (djiError == null ? "Successfully" : djiError.getDescription()));
            });
        }
    }

    /**
     * Resume mission
     */
    public void resumeMission() {
        if (mFlightController != null) {
            getWaypointMissionOperator().resumeMission((DJIError djiError) -> {
                showToast("Mission resume : " + (djiError == null ? "Successfully" : djiError.getDescription()));
            });
        }
    }

    /**
     * Pause mission
     */
    public void pauseMission() {
        if (mFlightController != null) {
            getWaypointMissionOperator().pauseMission((DJIError djiError) -> {
                showToast("Mission pause : " + (djiError == null ? "Successfully" : djiError.getDescription()));
            });
        }
    }

    /**
     * Start landing
     */
    public void land() {
        if (mFlightController != null) {
            mFlightController.startLanding((DJIError djiError) -> {
                showToast("Start landing : " + (djiError == null ? "Successfully" : djiError.getDescription()));
            });
        }
    }

    /**
     * Go home
     */
    public void goHome() {
        if (mFlightController != null) {
            mFlightController.getHomeLocation(new CommonCallbacks.CompletionCallbackWith<LocationCoordinate2D>() {
                @Override
                public void onSuccess(LocationCoordinate2D locationCoordinate2D) {
                    mFlightController.startGoHome(djiError -> {
                        showToast("Start go home : " + (djiError == null ? "Successfully" : djiError.getDescription()));
                    });
                }

                @Override
                public void onFailure(DJIError djiError) {
                    showToast("Start go home : " + djiError.getDescription());
                }
            });
        }
    }
}
