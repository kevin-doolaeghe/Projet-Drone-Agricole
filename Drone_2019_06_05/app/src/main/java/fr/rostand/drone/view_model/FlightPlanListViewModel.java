package fr.rostand.drone.view_model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;

import fr.rostand.drone.model.FlightPlan;
import fr.rostand.drone.repository.FlightPlanListRepository;

public class FlightPlanListViewModel extends AndroidViewModel {
    private static final String TAG = "FlightPlanListViewModel";

    private LiveData<List<FlightPlan>> mFlightPlanList;
    private FlightPlanListRepository mFlightPlanListRepository;

    public FlightPlanListViewModel(@NonNull Application application) {
        super(application);

        mFlightPlanListRepository = FlightPlanListRepository.getInstance(getApplication());
        mFlightPlanList = mFlightPlanListRepository.getFlightPlanList();
    }

    public void refresh() {
        mFlightPlanListRepository.getFlightPlanList();
    }

    public LiveData<List<FlightPlan>> getFlightPlanList() {
        Log.d(TAG, mFlightPlanList.getValue().toString());
        return mFlightPlanList;
    }

    public void addFlightPlan(FlightPlan flightPlan) {
        mFlightPlanListRepository.addFlightPlan(flightPlan);
    }

    public void editFlightPlan(FlightPlan flightPlan) {
        mFlightPlanListRepository.editFlightPlan(flightPlan);
    }

    public void deleteFlightPlan(FlightPlan flightPlan) {
        mFlightPlanListRepository.deleteFlightPlan(flightPlan);
    }
}
