package fr.rostand.drone.view_model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.util.List;

import dji.sdk.media.MediaFile;
import fr.rostand.drone.model.DroneMediaManager;

public class DroneMediaManagerViewModel extends AndroidViewModel {
    private static final String TAG = "DroneMediaManagerViewMo";

    private LiveData<List<MediaFile>> mMediaFileList;
    private LiveData<List<Bitmap>> mImageList;
    private DroneMediaManager mDroneMediaManager;

    public DroneMediaManagerViewModel(@NonNull Application application) {
        super(application);

        mDroneMediaManager = DroneMediaManager.getInstance(getApplication());
        mMediaFileList = mDroneMediaManager.getMediaFileList();
        mImageList = new MutableLiveData<>();
    }

    public void initComponents() {
        mDroneMediaManager.initComponents();
    }

    public void destroyComponents() {
        mDroneMediaManager.destroyComponents();
    }

    public LiveData<List<MediaFile>> getMediaFileList() {
        return mMediaFileList;
    }

    public LiveData<List<Bitmap>> getImageList() {
        return mImageList;
    }
}
