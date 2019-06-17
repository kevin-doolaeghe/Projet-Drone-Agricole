package fr.rostand.drone.view_model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

import dji.common.mission.waypoint.Waypoint;
import fr.rostand.drone.model.DroneFlightController;

public class DroneFlightControllerViewModel extends AndroidViewModel {
    private static final String TAG = "DroneFlightControllerVi";

    private LiveData<List<Waypoint>> mWaypointList;
    private LiveData<String> mLocation;
    private LiveData<String> mMission;
    private LiveData<String> mStatus;
    private LiveData<Integer> mProgress;
    private DroneFlightController mDroneFlightController;

    public DroneFlightControllerViewModel(@NonNull Application application) {
        super(application);

        mDroneFlightController = DroneFlightController.getInstance(getApplication());
        mWaypointList = mDroneFlightController.getWaypointList();
        mLocation = mDroneFlightController.getLocation();
        mMission = mDroneFlightController.getMission();
        mStatus = mDroneFlightController.getStatus();
        mProgress = mDroneFlightController.getProgress();
    }

    public void initComponents() {
        mDroneFlightController.initComponents();
    }

    public LiveData<List<Waypoint>> getWaypointList() {
        return mWaypointList;
    }

    public LiveData<String> getLocation() {
        return mLocation;
    }

    public LiveData<String> getMission() {
        return mMission;
    }

    public LiveData<String> getStatus() {
        return mStatus;
    }

    public LiveData<Integer> getProgress() {
        return mProgress;
    }

    public void generateWaypointList(double lat1, double lon1, double lat2, double lon2) {
        mDroneFlightController.generateWaypointList(lat1, lon1, lat2, lon2);
    }

    public boolean createMission() {
        return mDroneFlightController.createMission();
    }

    public void uploadMission() {
        mDroneFlightController.uploadMission();
    }

    public void startMission() {
        mDroneFlightController.startMission();
    }

    public void stopMission() {
        mDroneFlightController.stopMission();
    }

    public void resumeMission() {
        mDroneFlightController.resumeMission();
    }

    public void pauseMission() {
        mDroneFlightController.pauseMission();
    }

    public void land() {
        mDroneFlightController.land();
    }

    public void goHome() {
        mDroneFlightController.goHome();
    }
}
