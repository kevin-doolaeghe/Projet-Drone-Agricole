package fr.rostand.drone.view_model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import fr.rostand.drone.model.MissionImage;
import fr.rostand.drone.repository.MissionImageRepository;

public class MissionImageViewModel extends AndroidViewModel {
    private static final String TAG = "MissionImageViewModel";

    private MutableLiveData<List<MissionImage>> mMissionImageList;
    private MissionImageRepository mMissionImageRepository;

    public MissionImageViewModel(@NonNull Application application) {
        super(application);

        mMissionImageRepository = MissionImageRepository.getInstance(getApplication());
        mMissionImageList = new MutableLiveData<>();
        setImageList(new ArrayList<>());
    }

    public void setImageList(List<MissionImage> missionImageList) {
        mMissionImageList.setValue(missionImageList);
    }

    public void uploadImageList() {
        mMissionImageRepository.uploadImageList(mMissionImageList.getValue());
    }

    public void deleteImageList() {
        mMissionImageRepository.deleteImageList();
        setImageList(new ArrayList<>());
    }

    public void deleteFinalImage() {
        mMissionImageRepository.deleteFinalImage();
    }
}
