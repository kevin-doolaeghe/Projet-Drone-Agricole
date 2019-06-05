package fr.rostand.drone.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.rostand.drone.model.FlightPlan;
import fr.rostand.drone.util.VolleyRequestQueue;
import fr.rostand.drone.view.MainActivity;

public class FlightPlanListRepository {
    private static final String TAG = "FlightPlanListRepositor";

    private static FlightPlanListRepository instance;

    private Context mContext;
    private SharedPreferences mSharedPreferences;

    private MutableLiveData<List<FlightPlan>> mFlightPlanList;

    private FlightPlanListRepository(Context context) {
        mFlightPlanList = new MutableLiveData<>();

        mContext = context;
        mSharedPreferences = context.getSharedPreferences(MainActivity.SHARED_PREFERENCES_ID, 0);
    }

    public static FlightPlanListRepository getInstance(Context context) {
        if (instance == null) {
            instance = new FlightPlanListRepository(context);
        }
        return instance;
    }

    public LiveData<List<FlightPlan>> getFlightPlanList() {
        String baseUrl = mSharedPreferences.getString("baseUrl", VolleyRequestQueue.BASE_URL);

        StringRequest request = new StringRequest(
                Request.Method.GET,
                baseUrl + "plan/list",
                response -> {
                    Log.d(TAG, response);

                    FlightPlan[] flightPlanList = new Gson().fromJson(response, FlightPlan[].class);
                    mFlightPlanList.setValue(Arrays.asList(flightPlanList));
                },
                error -> {

                });

        VolleyRequestQueue.getInstance(mContext).addToRequestQueue(request);

        if (mFlightPlanList.getValue() == null) {
            mFlightPlanList.setValue(new ArrayList<>(Arrays.asList(new FlightPlan())));
        }
        return mFlightPlanList;
    }

    public LiveData<List<FlightPlan>> addFlightPlan(FlightPlan flightPlan) {
        String baseUrl = mSharedPreferences.getString("baseUrl", VolleyRequestQueue.BASE_URL);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                baseUrl + "plan/add",
                response -> {
                    Log.d(TAG, response);

                    FlightPlan[] flightPlanList = new Gson().fromJson(response, FlightPlan[].class);
                    mFlightPlanList.setValue(Arrays.asList(flightPlanList));
                },
                error -> {

                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id", Long.toString(flightPlan.getId()));
                params.put("name", flightPlan.getName());
                params.put("lat1", Double.toString(flightPlan.getLat1()));
                params.put("lon1", Double.toString(flightPlan.getLon1()));
                params.put("lat2", Double.toString(flightPlan.getLat2()));
                params.put("lon2", Double.toString(flightPlan.getLon2()));

                return params;
            }
        };

        VolleyRequestQueue.getInstance(mContext).addToRequestQueue(request);

        if (mFlightPlanList.getValue() == null) {
            mFlightPlanList.setValue(new ArrayList<>(Arrays.asList(new FlightPlan())));
        }
        return mFlightPlanList;
    }

    public LiveData<List<FlightPlan>> editFlightPlan(FlightPlan flightPlan) {
        String baseUrl = mSharedPreferences.getString("baseUrl", VolleyRequestQueue.BASE_URL);

        StringRequest request = new StringRequest(
                Request.Method.PUT,
                baseUrl + "plan/update/" + flightPlan.getId(),
                response -> {
                    Log.d(TAG, response);

                    FlightPlan[] flightPlanList = new Gson().fromJson(response, FlightPlan[].class);
                    mFlightPlanList.setValue(Arrays.asList(flightPlanList));
                },
                error -> {

                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id", Long.toString(flightPlan.getId()));
                params.put("name", flightPlan.getName());
                params.put("lat1", Double.toString(flightPlan.getLat1()));
                params.put("lon1", Double.toString(flightPlan.getLon1()));
                params.put("lat2", Double.toString(flightPlan.getLat2()));
                params.put("lon2", Double.toString(flightPlan.getLon2()));

                return params;
            }
        };

        VolleyRequestQueue.getInstance(mContext).addToRequestQueue(request);

        if (mFlightPlanList.getValue() == null) {
            mFlightPlanList.setValue(new ArrayList<>(Arrays.asList(new FlightPlan())));
        }
        return mFlightPlanList;
    }

    public LiveData<List<FlightPlan>> deleteFlightPlan(FlightPlan flightPlan) {
        String baseUrl = mSharedPreferences.getString("baseUrl", VolleyRequestQueue.BASE_URL);

        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                baseUrl + "plan/delete/" + flightPlan.getId(),
                response -> {
                    Log.d(TAG, response);

                    FlightPlan[] flightPlanList = new Gson().fromJson(response, FlightPlan[].class);
                    mFlightPlanList.setValue(Arrays.asList(flightPlanList));
                },
                error -> {

                });

        VolleyRequestQueue.getInstance(mContext).addToRequestQueue(request);

        if (mFlightPlanList.getValue() == null) {
            mFlightPlanList.setValue(new ArrayList<>(Arrays.asList(new FlightPlan())));
        }
        return mFlightPlanList;
    }
}
