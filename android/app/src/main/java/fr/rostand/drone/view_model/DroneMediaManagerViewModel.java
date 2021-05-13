package fr.rostand.drone.view_model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.util.List;

import fr.rostand.drone.model.DroneMediaManager;

public class DroneMediaManagerViewModel extends AndroidViewModel {
    private static final String TAG = "DroneMediaManagerViewMo";

    private LiveData<List<Bitmap>> mImageList;
    private DroneMediaManager mDroneMediaManager;

    public DroneMediaManagerViewModel(@NonNull Application application) {
        super(application);

        mDroneMediaManager = DroneMediaManager.getInstance(getApplication());
        mImageList = mDroneMediaManager.getImageList();
    }

    public void initComponents() {
        mDroneMediaManager.initComponents();
    }

    public void destroyComponents() {
        mDroneMediaManager.destroyComponents();
    }

    public LiveData<List<Bitmap>> getImageList() {
        return mImageList;
    }

    public void refreshFileList() {
        mDroneMediaManager.getFileList();
    }

    public void deleteFileList() {
        mDroneMediaManager.deleteFileList();
    }
}
