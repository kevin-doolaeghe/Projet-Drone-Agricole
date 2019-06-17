package fr.rostand.drone.view_model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import fr.rostand.drone.repository.FinalImageRepository;

public class FinalImageViewModel extends AndroidViewModel {
    private static final String TAG = "FinalImageViewModel";

    private LiveData<Bitmap> mFinalImage;
    private FinalImageRepository mFinalImageRepository;

    public FinalImageViewModel(@NonNull Application application) {
        super(application);

        mFinalImageRepository = FinalImageRepository.getInstance(getApplication());
        mFinalImage = mFinalImageRepository.getFinalImage();
    }

    public void refresh() {
        mFinalImageRepository.getFinalImage();
    }

    public LiveData<Bitmap> getFinalImage() {
        return mFinalImage;
    }
}
