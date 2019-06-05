package fr.rostand.drone.model;

import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.products.Aircraft;
import dji.sdk.products.HandHeld;
import dji.sdk.sdkmanager.DJISDKManager;

public class Drone {
    private static BaseProduct sProduct;
    private static WaypointMissionOperator sWaypointMissionOperator;

    public static synchronized BaseProduct getProductInstance() {
        if (null == sProduct) {
            sProduct = DJISDKManager.getInstance().getProduct();
        }
        return sProduct;
    }

    public static synchronized Aircraft getAircraftInstance() {
        if (!isAircraftConnected()) {
            return null;
        }
        return (Aircraft) getProductInstance();
    }

    public static synchronized Camera getCameraInstance() {
        if (getProductInstance() == null) {
            return null;
        }

        Camera camera = null;
        if (getProductInstance() instanceof Aircraft){
            camera = getProductInstance().getCamera();
        } else if (getProductInstance() instanceof HandHeld) {
            camera = getProductInstance().getCamera();
        }
        return camera;
    }

    public static synchronized WaypointMissionOperator getWaypointMissionOperator() {
        if (sWaypointMissionOperator == null) {
            if (DJISDKManager.getInstance().getMissionControl() != null){
                sWaypointMissionOperator = DJISDKManager.getInstance().getMissionControl().getWaypointMissionOperator();
            }
        }
        return sWaypointMissionOperator;
    }

    public static boolean isAircraftConnected() {
        return getProductInstance() != null && getProductInstance() instanceof Aircraft;
    }
}
