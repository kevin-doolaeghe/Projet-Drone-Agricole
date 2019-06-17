package fr.rostand.drone.view_model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import fr.rostand.drone.model.FlightPlan;
import fr.rostand.drone.repository.FlightPlanRepository;

public class FlightPlanViewModel extends AndroidViewModel {
    private static final String TAG = "FlightPlanViewModel";

    private MutableLiveData<FlightPlan> mFlightPlan;
    private FlightPlanRepository mFlightPlanRepository;

    public FlightPlanViewModel(@NonNull Application application) {
        super(application);

        mFlightPlanRepository = FlightPlanRepository.getInstance(getApplication());
        mFlightPlan = new MutableLiveData<>();
    }

    public void setFlightPlan(FlightPlan flightPlan) {
        mFlightPlan.setValue(flightPlan);
    }

    public LiveData<FlightPlan> getFlightPlan() {
        return mFlightPlan;
    }

    public void addDataToFlightPlan(long latImgNb, long lonImgNb) {
        mFlightPlanRepository.addDataToFlightPlan(latImgNb, lonImgNb);
    }

    public void startAnalysis() {
        mFlightPlanRepository.startAnalysis();
    }
}
