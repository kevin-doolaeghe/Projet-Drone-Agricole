package fr.rostand.drone.model;

import android.content.Context;
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

public class DroneFlightController {
    private static final String TAG = "DroneFlightController";

    private Context mContext;
    private static DroneFlightController instance;

    // Drone variables
    private FlightController mFlightController;

    private WaypointMissionOperatorListener mWaypointMissionOperatorListener = new WaypointMissionOperatorListener() {
        @Override
        public void onDownloadUpdate(@NonNull WaypointMissionDownloadEvent waypointMissionDownloadEvent) {

        }

        @Override
        public void onUploadUpdate(@NonNull WaypointMissionUploadEvent waypointMissionUploadEvent) {

        }

        @Override
        public void onExecutionUpdate(@NonNull WaypointMissionExecutionEvent waypointMissionExecutionEvent) {
            int percent = (int) ((((double) waypointMissionExecutionEvent.getProgress().targetWaypointIndex) / ((double) waypointMissionExecutionEvent.getProgress().totalWaypointCount)) * 100);
            showToast("Mission completed at : " + percent + "%");
            mMissionProgressPercentage = percent;
        }

        @Override
        public void onExecutionStart() {
            mMissionProgressPercentage = 0;
        }

        @Override
        public void onExecutionFinish(@Nullable DJIError djiError) {
            showToast("Finish :)");
            mMissionProgressPercentage = 0;
        }
    };
    private GPSSignalLevel mGPSSignalLevel;
    private double mCurrentLatitude = 181;
    private double mCurrentLongitude = 181;

    // Settings
    private float mMinAltitude = 10.0f;
    private float mMaxAltitude = 15.0f;
    private float mSpeed = 2.0f;

    // Mission
    private List<Waypoint> mWaypointList = new ArrayList<>();

    // Information
    private String mLocation = "";
    private String mMissionSummary = "";
    private String mMissionStatus = "";
    private int mMissionProgressPercentage = 0;

    /**
     * Constructor
     */
    private DroneFlightController(Context context) {
        this.mContext = context;
    }

    public static DroneFlightController getInstance(Context context) {
        if (instance == null) {
            instance = new DroneFlightController(context);
        }
        return instance;
    }

    /**
     * Getters and setters
     */
    public WaypointMissionOperatorListener getWaypointMissionOperatorListener() {
        return mWaypointMissionOperatorListener;
    }

    public void setWaypointMissionOperatorListener(WaypointMissionOperatorListener waypointMissionOperatorListener) {
        mWaypointMissionOperatorListener = waypointMissionOperatorListener;
    }

    public float getMinAltitude() {
        return mMinAltitude;
    }

    public void setMinAltitude(float minAltitude) {
        mMinAltitude = minAltitude;
    }

    public float getMaxAltitude() {
        return mMaxAltitude;
    }

    public void setMaxAltitude(float maxAltitude) {
        mMaxAltitude = maxAltitude;
    }

    public float getSpeed() {
        return mSpeed;
    }

    public void setSpeed(float speed) {
        mSpeed = speed;
    }

    public List<Waypoint> getWaypointList() {
        return mWaypointList;
    }

    public String getLocation() {
        return mLocation;
    }

    public String getMissionSummary() {
        return mMissionSummary;
    }

    public String getMissionStatus() {
        return mMissionStatus;
    }

    public int getMissionProgress() {
        return mMissionProgressPercentage;
    }

    /**
     * Handling methods
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

    private void updateDroneLocation() {
        mLocation = R.string.title_gps_signal + " " + mGPSSignalLevel + "\n" + R.string.title_latitude + " " + mCurrentLatitude + "\n" + R.string.title_longitude + " " + mCurrentLongitude;
    }

    private boolean checkGpsCoordination(double latitude, double longitude) {
        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f && longitude != 0f);
    }

    private WaypointMissionOperator getWaypointMissionOperator() {
        return Drone.getWaypointMissionOperator();
    }

    private void showToast(final String toastMsg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(mContext, toastMsg, Toast.LENGTH_SHORT).show());
    }

    /**
     * Drone management methods
     */
    public boolean createMission(double lat1, double lon1, double lat2, double lon2) {
        mMissionSummary = "";
        mMissionStatus = "";

        mWaypointList = new ArrayList<>();

        if (!checkGpsCoordination(lat1, lon1) || !checkGpsCoordination(lat2, lon2)) {
            mMissionSummary = "No mission created !";
            mMissionStatus = "Wrong coordinates !";
            return false;
        }

        if (mFlightController != null) {
            mCurrentLatitude = mFlightController.getState().getAircraftLocation().getLatitude();
            mCurrentLongitude = mFlightController.getState().getAircraftLocation().getLongitude();

            if (Double.isNaN(mCurrentLatitude) || Double.isNaN(mCurrentLongitude) && !checkGpsCoordination(mCurrentLatitude, mCurrentLongitude)) {
                mMissionSummary = "No mission created !";
                mMissionStatus = "Le GPS n'est pas prêt !";
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
                    .autoFlightSpeed(mSpeed)
                    .maxFlightSpeed(mSpeed)
                    .flightPathMode(WaypointMissionFlightPathMode.NORMAL);

            //------------------------------------------------------------------------------------------

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

            float altitude = mMinAltitude;

            double ratio = 1.78;

            double latGap = lat2 - lat1;
            double lonGap = lon2 - lon1;

            double latGapLength = latGap * 111319;
            double lonGapLength = lonGap * 111319;

            int nbImgMax = 99;
            int mLatAxisImgCount;
            int mLonAxisImgCount;
            int mTotalImgCount;

            double imgHeight;
            double imgWidth;

            boolean running = true;

            while (running) {
                imgHeight = altitude * 7.7 * 5.6 / 20 / Math.sqrt(1 + ratio * ratio);
                imgWidth = imgHeight * ratio;

                Log.d(TAG, "Image :\nHeight : " + imgHeight + "m | Width : " + imgWidth + "m");

                mLatAxisImgCount = (int) Math.ceil(latGapLength / imgHeight); // Aircraft is facing north
                mLonAxisImgCount = (int) Math.ceil(lonGapLength / imgWidth);
                mTotalImgCount = mLatAxisImgCount * mLonAxisImgCount;

                if (mTotalImgCount <= nbImgMax) {
                    Waypoint homePoint = new Waypoint(mCurrentLatitude, mCurrentLongitude, altitude);

                    mWaypointList.add(homePoint);

                    double latToAdd = imgHeight  / 111319;
                    double lonToAdd = imgWidth / 111319;

                    for (int i = 0; i < mLonAxisImgCount; i++) {
                        for (int j = 0; j < mLatAxisImgCount; j++) {
                            double latitude;
                            double longitude;

                            if (i % 2 == 0) {
                                latitude = lat1 + (j + 0.5) * latToAdd;
                            } else {
                                latitude = lat1 + (mLatAxisImgCount - j - 0.5) * latToAdd;
                            }
                            longitude = lon1 + (i + 0.5) * lonToAdd;

                            mWaypointList.add(new Waypoint(latitude, longitude, altitude));
                        }
                    }

                    mWaypointList.add(homePoint);

                    running = false;
                    mMissionSummary = "Calcul du parcours terminé avec succès !!" + "\nAltitude de vol : " + altitude + "m" + "\nImages nécessaires : "
                            + mTotalImgCount + " (" + mLatAxisImgCount + "x" + mLonAxisImgCount + ")";
                } else {
                    if (altitude == mMaxAltitude) {
                        mMissionSummary = "No mission created !";
                        mMissionStatus = "Zone à cartographier trop grande !";
                        return false;
                    } else {
                        altitude++;
                    }
                }
            }

            //------------------------------------------------------------------------------------------

            waypointMissionBuilder.waypointList(mWaypointList).waypointCount(mWaypointList.size());
            waypointMissionBuilder.setExitMissionOnRCSignalLostEnabled(true);

            if (waypointMissionBuilder.getWaypointList().size() > 0) {
                for (int i = 0; i < waypointMissionBuilder.getWaypointList().size(); i++) {
                    waypointMissionBuilder.getWaypointList().get(i).altitude = altitude;
                    if(i != 0 && i != waypointMissionBuilder.getWaypointList().size() - 1) {
                        waypointMissionBuilder.getWaypointList().get(i).addAction(new WaypointAction(WaypointActionType.GIMBAL_PITCH, -90)); // Gimbal's pitch at -90°
                        waypointMissionBuilder.getWaypointList().get(i).addAction(new WaypointAction(WaypointActionType.STAY, 500)); // Wait 500ms
                        waypointMissionBuilder.getWaypointList().get(i).addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 0)); // Take a photo
                    }
                }
            }

            getWaypointMissionOperator().cancelUploadingMission(null);
            getWaypointMissionOperator().clearMission();
            getWaypointMissionOperator().addListener(mWaypointMissionOperatorListener);

            DJIError error = getWaypointMissionOperator().loadMission(waypointMissionBuilder.build());
            if (error == null) {
                showToast("Waypoint load succeeded :)");
                mMissionStatus = getWaypointMissionOperator().getLoadedMission().getWaypointCount() + " waypoints loaded";
            } else {
                showToast("Waypoint load failed !" + error.getDescription());
                mMissionSummary = "No mission created !";
                mMissionStatus = "Error while loading..";
                mWaypointList.clear();
                return false;
            }
            return true;
        } else {
            mMissionSummary = "No mission created !";
            mMissionStatus = "Drone not connected";
            return false;
        }
    }

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

    public void startMission() {
        if (mFlightController != null) {
            getWaypointMissionOperator().startMission((DJIError djiError) -> {
                showToast("Mission start : " + (djiError == null ? "Successfully" : djiError.getDescription()));
            });
        }
    }

    public void stopMission() {
        if (mFlightController != null) {
            getWaypointMissionOperator().stopMission((DJIError djiError) -> {
                showToast("Mission stop : " + (djiError == null ? "Successfully" : djiError.getDescription()));
            });
        }
    }

    public void resumeMission() {
        if (mFlightController != null) {
            getWaypointMissionOperator().resumeMission((DJIError djiError) -> {
                showToast("Mission resume : " + (djiError == null ? "Successfully" : djiError.getDescription()));
            });
        }
    }

    public void pauseMission() {
        if (mFlightController != null) {
            getWaypointMissionOperator().pauseMission((DJIError djiError) -> {
                showToast("Mission pause : " + (djiError == null ? "Successfully" : djiError.getDescription()));
            });
        }
    }

    public void land() {
        if (mFlightController != null) {
            mFlightController.startLanding((DJIError djiError) -> {
                showToast("Start landing : " + (djiError == null ? "Successfully" : djiError.getDescription()));
            });
        }
    }

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
